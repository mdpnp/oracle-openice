package org.mdpnp.hiberdds.testapp;

import java.sql.Connection;
import java.sql.SQLException;

import org.mdpnp.rtiapi.data.QosProfiles;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.ViewStateKind;

public class NumericUtil extends AbstractIceType {
    private ice.NumericDataReader reader;
    public void register(final DomainParticipant participant, final Subscriber subscriber) {
        ice.NumericTypeSupport.register_type(participant, ice.NumericTypeSupport.get_type_name());
        topic = participant.create_topic(ice.NumericTopic.VALUE, ice.NumericTypeSupport.get_type_name(),
                DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        reader = (ice.NumericDataReader) subscriber.create_datareader_with_profile(topic, QosProfiles.ice_library,
                QosProfiles.numeric_data, null, StatusKind.STATUS_MASK_NONE);
    }
    
    public void prepare(final Connection conn) throws SQLException {
        insertSample = conn.prepareStatement("INSERT INTO NUMERIC_SAMPLE (NUMERIC_SAMPLE_ID, NUMERIC_ID, SOURCE_TIME, VALUE) VALUES (NUMERIC_SAMPLE_SEQ.NEXTVAL, ?, ?, ?)");
        insertUpdateInstance = conn.prepareCall("{? = call INSERT_UPDATE_NUMERIC (?, ?, ?, ?, ?)}");
        insertUpdateInstance.registerOutParameter(1, java.sql.Types.INTEGER);
    }
    
    private int batchUpdate(final ice.NumericDataReader reader, final ice.NumericSeq numericSequence, final SampleInfoSeq sampleInfoSequence) throws SQLException {
        insertSample.clearBatch();
        final int sz = sampleInfoSequence.size();
        for(int i = 0; i < sz; i++) {
            
            SampleInfo si = (SampleInfo) sampleInfoSequence.get(i);
            ice.Numeric n = (ice.Numeric) numericSequence.get(i);
            
            Long persistedNumeric = instances.getIfPresent(si.instance_handle);
            
            if(!si.valid_data) {
                reader.get_key_value(n, si.instance_handle);
            }
            
            if(null == persistedNumeric) {
                insertUpdateInstance.clearParameters();
                insertUpdateInstance.setString(2, n.unique_device_identifier);
                insertUpdateInstance.setString(3, n.metric_id);
                insertUpdateInstance.setString(4, n.vendor_metric_id);
                insertUpdateInstance.setInt(5, n.instance_id);
                insertUpdateInstance.setString(6, n.unit_id);
                insertUpdateInstance.execute();

                persistedNumeric = insertUpdateInstance.getLong(1);
                
                System.err.println("From DB this instance is " + persistedNumeric);
                instances.put(new InstanceHandle_t(si.instance_handle), persistedNumeric);
            }
            
            if(si.valid_data) {
                insertSample.clearParameters();
                insertSample.setLong(1, persistedNumeric);
//                insertNumericSample.setTimestamp(2, new java.sql.Timestamp(n.device_time.sec * 1000L + n.device_time.nanosec / 1000000L));
//                insertNumericSample.setTimestamp(3, new java.sql.Timestamp(n.presentation_time.sec * 1000L + n.presentation_time.nanosec / 1000000L));
                insertSample.setTimestamp(2, new java.sql.Timestamp(si.source_timestamp.sec * 1000L + si.source_timestamp.nanosec / 1000000L));
                // TODO this should be a floating point number
                insertSample.setFloat(3, n.value);
                insertSample.addBatch();
            }
        }
        insertSample.executeBatch();
        return sz;
    }
    
    public void close(final DomainParticipant participant, Subscriber subscriber) throws SQLException {
        reader.delete_contained_entities();
        subscriber.delete_datareader(reader);
        participant.delete_topic(topic);
        ice.NumericTypeSupport.unregister_type(participant, ice.NumericTypeSupport.get_type_name());
        
        insertUpdateInstance.close();
        insertSample.close();
    }
    
    private final ice.NumericSeq numericSequence = new ice.NumericSeq();
    private final SampleInfoSeq  sampleInfoSequence = new SampleInfoSeq();
    
    public int poll() {
        int size = 0;
        try {
            reader.take(numericSequence, sampleInfoSequence, ResourceLimitsQosPolicy.LENGTH_UNLIMITED, SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE, InstanceStateKind.ANY_INSTANCE_STATE);
            final int sz = size = sampleInfoSequence.size();
            System.err.println("POLL "+sz+" samples");
            batchUpdate(reader, numericSequence, sampleInfoSequence);
            insertSample.getConnection().commit();
        } catch (RETCODE_NO_DATA noData) {
            // TODO is it better to rollback or commit an empty transaction? 
            return 0;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        } finally {
            reader.return_loan(numericSequence, sampleInfoSequence);

        }
        return size;
    }
}
