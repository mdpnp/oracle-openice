package org.mdpnp.hiberdds.testapp;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.mdpnp.devices.IceQos;
import org.mdpnp.hiberdds.mappings.Numeric;
import org.mdpnp.hiberdds.mappings.NumericSample;
import org.mdpnp.hiberdds.util.HibernateUtil;
import org.mdpnp.rtiapi.data.QosProfiles;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderListener;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.LivelinessChangedStatus;
import com.rti.dds.subscription.RequestedDeadlineMissedStatus;
import com.rti.dds.subscription.RequestedIncompatibleQosStatus;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleLostStatus;
import com.rti.dds.subscription.SampleRejectedStatus;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.SubscriberQos;
import com.rti.dds.subscription.SubscriptionMatchedStatus;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.topic.Topic;

public class DDSWriter implements DataReaderListener {
   
    
    public static void main(String[] args) throws IOException {
        IceQos.loadAndSetIceQos();
        new DDSWriter().process();
    }
    
    private Session session;
    
    public void process() throws IOException {
        session = HibernateUtil.getSessionFactory().openSession();
        
        
        DomainParticipant participant = DomainParticipantFactory.get_instance().create_participant(0, 
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE);
        
        ice.NumericTypeSupport.register_type(participant, ice.NumericTypeSupport.get_type_name());
        
        Topic topic = participant.create_topic(ice.NumericTopic.VALUE, ice.NumericTypeSupport.get_type_name(),
                DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        
        Subscriber subscriber = participant.create_subscriber(DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        
        SubscriberQos sQos = new SubscriberQos();
        subscriber.get_qos(sQos);
        sQos.partition.name.add("*");
        subscriber.set_qos(sQos);
        
        System.err.println("ALL SET, STARTING");
        ice.NumericDataReader reader = (ice.NumericDataReader) subscriber.create_datareader_with_profile(topic, QosProfiles.ice_library,
                QosProfiles.numeric_data, this, StatusKind.DATA_AVAILABLE_STATUS);
        
        System.in.read();
        
        subscriber.delete_datareader(reader);
        participant.delete_subscriber(subscriber);
        participant.delete_topic(topic);
        DomainParticipantFactory.get_instance().delete_participant(participant);
        
        
        
        
        
        
        session.close();
        
    }

    
    private final ice.NumericSeq numericSequence = new ice.NumericSeq();
    private final SampleInfoSeq  sampleInfoSequence = new SampleInfoSeq();
    
    // TODO This should be maintaining soft references
    private final Map<InstanceHandle_t, Numeric> instances = new HashMap<>();
    
    // TODO THIS PROBABLY SHOULD BE USING A POLLING STRATEGY
    @Override
    public void on_data_available(DataReader reader) {
        ice.NumericDataReader nReader = (ice.NumericDataReader) reader;
        session.beginTransaction();
        for(;;) {
            try {
                nReader.take(numericSequence, sampleInfoSequence, ResourceLimitsQosPolicy.LENGTH_UNLIMITED, SampleStateKind.ANY_SAMPLE_STATE,
                        ViewStateKind.ANY_VIEW_STATE, InstanceStateKind.ANY_INSTANCE_STATE);
                final int sz = sampleInfoSequence.size();
                System.err.println("Handling " + sz + " samples");
                for(int i = 0; i < sz; i++) {
                    SampleInfo si = (SampleInfo) sampleInfoSequence.get(i);
                    ice.Numeric n = (ice.Numeric) numericSequence.get(i);
                    
                    Numeric persistedNumeric = instances.get(si.instance_handle);
                    
                    if(null == persistedNumeric) {
                        // TODO Query the database
                        persistedNumeric = new Numeric();
                        persistedNumeric.setInstance_id(n.instance_id);
                        persistedNumeric.setMetric_id(n.metric_id);
                        persistedNumeric.setUnique_device_identifier(n.unique_device_identifier);
                        persistedNumeric.setUnit_id(n.unit_id);
                        persistedNumeric.setVendor_metric_id(n.vendor_metric_id);
                        session.saveOrUpdate(persistedNumeric);
                        instances.put(new InstanceHandle_t(si.instance_handle), persistedNumeric);
                    }
                    
                    if(si.valid_data) {
                        NumericSample ns = new NumericSample();
                        ns.setNumeric(persistedNumeric);
                        ns.setDevice_time(new Date(n.device_time.sec * 1000L + n.device_time.nanosec / 1000000L));
                        ns.setPresentation_time(new Date(n.presentation_time.sec * 1000L + n.presentation_time.nanosec / 1000000L));
                        ns.setSource_time(new Date(si.source_timestamp.sec * 1000L + si.source_timestamp.nanosec / 1000000L));
                        // TODO this should be a floating point number
                        ns.setValue((int)n.value);
                        session.save(ns);
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                break;
            } finally {
                nReader.return_loan(numericSequence, sampleInfoSequence);
            }
        }
        // TODO should explicitly roll back on exception
        session.getTransaction().commit();
    }

    @Override
    public void on_liveliness_changed(DataReader arg0, LivelinessChangedStatus arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void on_requested_deadline_missed(DataReader arg0, RequestedDeadlineMissedStatus arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void on_requested_incompatible_qos(DataReader arg0, RequestedIncompatibleQosStatus arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void on_sample_lost(DataReader arg0, SampleLostStatus arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void on_sample_rejected(DataReader arg0, SampleRejectedStatus arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void on_subscription_matched(DataReader arg0, SubscriptionMatchedStatus arg1) {
        // TODO Auto-generated method stub
        
    }
}
