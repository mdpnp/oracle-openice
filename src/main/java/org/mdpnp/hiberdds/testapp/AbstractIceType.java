package org.mdpnp.hiberdds.testapp;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.mdpnp.rtiapi.data.QosProfiles;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.BadKind;
import com.rti.dds.infrastructure.Bounds;
import com.rti.dds.infrastructure.Copyable;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.topic.Topic;
import com.rti.dds.topic.TypeSupport;
import com.rti.dds.typecode.TypeCode;
import com.rti.dds.util.Sequence;

public abstract class AbstractIceType<R extends DataReader, D extends Copyable, S extends Sequence> implements IceType {
    protected CallableStatement insertUpdateInstance;
    protected PreparedStatement insertSample;
    protected Cache<InstanceHandle_t, Long> instances = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    protected Topic topic;
    protected final String topicString;
    protected final TypeCode typeCode;
    protected final Field[] dataFields;
    protected final boolean[] key;
    protected final Method register_type, get_type_name, return_loan, take, get_key_value;
    private R reader;
    private final String qosProfileName;
    
    private final S sequence;
    private final SampleInfoSeq  sampleInfoSequence = new SampleInfoSeq();    
    
    @SuppressWarnings("unchecked")
    public AbstractIceType(final String topicString, 
                           final Class<? extends D> dataClass,
                           final String qosProfileName) {
        this.topicString = topicString;
        this.qosProfileName = qosProfileName;
        final String typeName = dataClass.getName();
        try {
            final Class<? extends R> readerClass = (Class<? extends R>) Class.forName(typeName+"DataReader");
            final Class<? extends S> sequenceClass = (Class<? extends S>) Class.forName(typeName+"Seq");
            final Class<? extends TypeSupport> typeSupportClass = (Class<? extends TypeSupport>) Class.forName(typeName+"TypeSupport");
            final Class<?> typeCodeClass = Class.forName(typeName+"TypeCode");
            
            sequence = sequenceClass.newInstance();
            typeCode = (TypeCode) typeCodeClass.getField("VALUE").get(null);
            
            List<Field> _dataFields = new ArrayList<Field>();
            List<Boolean> _key = new ArrayList<Boolean>();
            
            for(int i = 0; i < typeCode.member_count(); i++) {
                if(null!=SchemaGen.oracletype(typeCode.member_type(i), false)) {
                    _dataFields.add(dataClass.getField(typeCode.member_name(i)));
                   _key.add(typeCode.is_member_key(i));
                }
            }
            
            dataFields = _dataFields.toArray(new Field[0]);
            key = new boolean[dataFields.length];
            for(int i = 0; i < key.length; i++) {
                key[i] = _key.get(i);
            }
            
            register_type = typeSupportClass.getMethod("register_type", DomainParticipant.class, String.class);
            get_type_name = typeSupportClass.getMethod("get_type_name");
            return_loan   = readerClass.getMethod("return_loan", sequenceClass, SampleInfoSeq.class);
            take          = readerClass.getMethod("take", sequenceClass, SampleInfoSeq.class, int.class, int.class, int.class, int.class);
            get_key_value = readerClass.getMethod("get_key_value", dataClass, InstanceHandle_t.class);
            
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException | BadKind | Bounds | NoSuchMethodException | ClassNotFoundException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
 
    public void prepare(final Connection conn) throws SQLException {
        String sqlName = SchemaGen.topicToSql(topicString);
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(sqlName).append("_SAMPLE (").append(sqlName).append("_SAMPLE_ID, ").append(sqlName).append("_ID, SOURCE_TIME");
        for(int i = 0; i < dataFields.length; i++) {
            if(!key[i]) {
                sb.append(", ").append(dataFields[i].getName().toUpperCase());
            }
        }
        sb.append(") VALUES (").append(sqlName).append("_SAMPLE_SEQ.NEXTVAL, ?, ?");
        for(int i = 0; i < dataFields.length; i++) {
            if(!key[i]) {
                sb.append(", ?");
            }
        }
        sb.append(")");
        System.err.println(topicString+" " +sb.toString());
        insertSample = conn.prepareStatement(sb.toString());
        
        sb.delete(0, sb.length());
        sb.append("{? = call UPSERT_").append(sqlName).append("(?");
        // assume at least one key field
        for(int i = 1; i < dataFields.length; i++) {
            if(key[i]) {
                sb.append(", ?");
            }
        }
        sb.append(")}");
        insertUpdateInstance = conn.prepareCall(sb.toString());
        insertUpdateInstance.registerOutParameter(1, java.sql.Types.INTEGER);
    }
    
    @SuppressWarnings("unchecked")
    public void register(final DomainParticipant participant, final Subscriber subscriber) {
        try {
            register_type.invoke(null, participant, get_type_name.invoke(null));
    
            topic = participant.create_topic(topicString, (String) get_type_name.invoke(null),
                    DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
            reader = (R) subscriber.create_datareader_with_profile(topic, QosProfiles.ice_library,
                    qosProfileName, null, StatusKind.STATUS_MASK_NONE);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void close(final DomainParticipant participant, Subscriber subscriber) throws SQLException {
        reader.delete_contained_entities();
        subscriber.delete_datareader(reader);
        participant.delete_topic(topic);
        // unregister_type ?
        insertUpdateInstance.close();
        insertSample.close();
    }    
    
    public int poll() {
        int size = 0;
        try {
            take.invoke(reader, sequence, sampleInfoSequence, ResourceLimitsQosPolicy.LENGTH_UNLIMITED, SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE, InstanceStateKind.ANY_INSTANCE_STATE);
            size = sampleInfoSequence.size();
           
            batchUpdate(reader, sequence, sampleInfoSequence);
            insertSample.getConnection().commit();
        } catch (RETCODE_NO_DATA noData) {
            return 0;
        } catch (InvocationTargetException ite) {
            if(!(ite.getCause() instanceof RETCODE_NO_DATA)) {
                System.err.println("For " +topicString);
                ite.printStackTrace();
            }
            return 0;

        } catch (Throwable throwable) {
            System.err.println("For " +topicString);
            throwable.printStackTrace();
            return 0;
        } finally {
            try {
                return_loan.invoke(reader, sequence, sampleInfoSequence);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

        }
        return size;
    }    
    
    @SuppressWarnings("unchecked")
    private int batchUpdate(final R reader, final S sequence, final SampleInfoSeq sampleInfoSequence) throws SQLException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        insertSample.clearBatch();
        final int sz = sampleInfoSequence.size();
        for(int i = 0; i < sz; i++) {
            
            SampleInfo si = (SampleInfo) sampleInfoSequence.get(i);
            D a = (D) sequence.get(i);
            
            Long persisted = instances.getIfPresent(si.instance_handle);
            
            if(!si.valid_data) {
                get_key_value.invoke(reader, a, si.instance_handle);
            }
            
            if(null == persisted) {
                insertUpdateInstance.clearParameters();
                int x = 2;
                for(int j = 0; j < dataFields.length; j++) {
                    if(key[j]) {
                        insertUpdateInstance.setObject(x++, standardType(dataFields[j].get(a)));
                    }
                }
                insertUpdateInstance.execute();

                persisted = insertUpdateInstance.getLong(1);
                
//                System.err.println("From DB this " + topicString + " instance is " + persisted);
                instances.put(new InstanceHandle_t(si.instance_handle), persisted);
            }
            
            if(si.valid_data) {
                insertSample.clearParameters();
                insertSample.setLong(1, persisted);
                insertSample.setTimestamp(2, new java.sql.Timestamp(si.source_timestamp.sec * 1000L + si.source_timestamp.nanosec / 1000000L));
                int x = 3;
                for(int j = 0; j < dataFields.length; j++) {
                    if(!key[j]) {
                        insertSample.setObject(x++, standardType(dataFields[j].get(a)));
                    }
                }                
                insertSample.addBatch();
            }
        }
        insertSample.executeBatch();
        return sz;
    }
    
    private Object standardType(Object o) {
        if(o instanceof ice.Time_t) {
            ice.Time_t t = (ice.Time_t)o;
            return new java.sql.Timestamp(t.sec*1000L+t.nanosec/1000000L);
        } else if(o instanceof com.rti.dds.util.Enum) {
            com.rti.dds.util.Enum e = (com.rti.dds.util.Enum) o;
            return e.name();
        } else {
            return o;
        }
    }
}
