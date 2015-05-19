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

public class AlertUtil extends AbstractIceType {
    private final String topicString;
    private ice.AlertDataReader reader;
    public void register(final DomainParticipant participant, final Subscriber subscriber) {
        ice.AlertTypeSupport.register_type(participant, ice.AlertTypeSupport.get_type_name());
        topic = participant.create_topic(topicString, ice.AlertTypeSupport.get_type_name(),
                DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        reader = (ice.AlertDataReader) subscriber.create_datareader_with_profile(topic, QosProfiles.ice_library,
                QosProfiles.numeric_data, null, StatusKind.STATUS_MASK_NONE);
    }
    
    public AlertUtil(final String topicString) {
        this.topicString = topicString;
    }
    
    public void prepare(final Connection conn) throws SQLException {
        String sqlName = topicString.toUpperCase();
        insertSample = conn.prepareStatement("INSERT INTO "+sqlName+"_SAMPLE ("+sqlName+"_SAMPLE_ID, "+sqlName+"_ID, SOURCE_TIME, TEXT) VALUES ("+sqlName+"_SAMPLE_SEQ.NEXTVAL, ?, ?, ?)");
        insertUpdateInstance = conn.prepareCall("{? = call INSERT_UPDATE_"+sqlName+" (?, ?)}");
        insertUpdateInstance.registerOutParameter(1, java.sql.Types.INTEGER);
    }
    
    private int batchUpdate(final ice.AlertDataReader reader, final ice.AlertSeq alertSequence, final SampleInfoSeq sampleInfoSequence) throws SQLException {
        insertSample.clearBatch();
        final int sz = sampleInfoSequence.size();
        for(int i = 0; i < sz; i++) {
            
            SampleInfo si = (SampleInfo) sampleInfoSequence.get(i);
            ice.Alert a = (ice.Alert) alertSequence.get(i);
            
            Long persistedAlert = instances.getIfPresent(si.instance_handle);
            
            if(!si.valid_data) {
                reader.get_key_value(a, si.instance_handle);
            }
            
            if(null == persistedAlert) {
                insertUpdateInstance.clearParameters();
                insertUpdateInstance.setString(2, a.unique_device_identifier);
                insertUpdateInstance.setString(3, a.identifier);
                insertUpdateInstance.execute();

                persistedAlert = insertUpdateInstance.getLong(1);
                
                System.err.println("From DB this instance is " + persistedAlert);
                instances.put(new InstanceHandle_t(si.instance_handle), persistedAlert);
            }
            
            if(si.valid_data) {
                insertSample.clearParameters();
                insertSample.setLong(1, persistedAlert);
                insertSample.setTimestamp(2, new java.sql.Timestamp(si.source_timestamp.sec * 1000L + si.source_timestamp.nanosec / 1000000L));
                insertSample.setString(3, a.text);
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
        // Ugh i'm so sick of dealing with this API
//        ice.AlertTypeSupport.unregister_type(participant, ice.AlertTypeSupport.get_type_name());
        
        insertUpdateInstance.close();
        insertSample.close();
    }
    
    private final ice.AlertSeq alertSequence = new ice.AlertSeq();
    private final SampleInfoSeq  sampleInfoSequence = new SampleInfoSeq();
    
    public int poll() {
        int size = 0;
        try {
            reader.take(alertSequence, sampleInfoSequence, ResourceLimitsQosPolicy.LENGTH_UNLIMITED, SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE, InstanceStateKind.ANY_INSTANCE_STATE);
            final int sz = size = sampleInfoSequence.size();
            System.err.println("POLL "+sz+" samples");
            batchUpdate(reader, alertSequence, sampleInfoSequence);
            insertSample.getConnection().commit();
        } catch (RETCODE_NO_DATA noData) {
            // TODO is it better to rollback or commit an empty transaction? 
            return 0;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        } finally {
            reader.return_loan(alertSequence, sampleInfoSequence);

        }
        return size;
    }
}
