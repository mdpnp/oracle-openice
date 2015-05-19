package org.mdpnp.hiberdds.testapp;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mdpnp.devices.IceQos;
import org.mdpnp.hiberdds.mappings.Numeric;
import org.mdpnp.hiberdds.mappings.NumericSample;
import org.mdpnp.hiberdds.util.HibernateUtil;
import org.mdpnp.rtiapi.data.QosProfiles;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.SubscriberQos;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.topic.Topic;

public class DDSWriter {
    
    
    public static void main(String[] args) throws IOException, InterruptedException {
        IceQos.loadAndSetIceQos();
        DDSWriter writer = new DDSWriter();
        writer.start();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> task = executor.scheduleAtFixedRate(()->writer.poll(), 1000L-System.currentTimeMillis()%1000L, 10000L, TimeUnit.MILLISECONDS);
        System.in.read();
        task.cancel(false);
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        writer.stop();
        System.exit(0);
    }
    
    private Session session;
    
    private final Base64.Decoder b64Decoder = Base64.getDecoder();
    private final Base64.Encoder b64Encoder = Base64.getEncoder();
    
    private DomainParticipant participant;
    private Topic topic;
    private Subscriber subscriber;
    private ice.NumericDataReader reader;
    
    public void start() {
        session = HibernateUtil.getSessionFactory().openSession();
        
        List<Numeric> nums = session.createQuery("from Numeric").list();
        for(Numeric n : nums) {
            System.err.println("LOADED INSTANCE " + n.getInstance_handle());
            instances.put(n.getInstance_handle(), n);
        }
        
        System.err.println("PRELOADED " + nums.size() + " numerics");

        
        participant = DomainParticipantFactory.get_instance().create_participant(0, 
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE);
        
        ice.NumericTypeSupport.register_type(participant, ice.NumericTypeSupport.get_type_name());
        
        topic = participant.create_topic(ice.NumericTopic.VALUE, ice.NumericTypeSupport.get_type_name(),
                DomainParticipant.TOPIC_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        
        subscriber = participant.create_subscriber(DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);
        
        SubscriberQos sQos = new SubscriberQos();
        subscriber.get_qos(sQos);
        sQos.partition.name.add("*");
        subscriber.set_qos(sQos);
        
        reader = (ice.NumericDataReader) subscriber.create_datareader_with_profile(topic, QosProfiles.ice_library,
                QosProfiles.numeric_data, null, StatusKind.STATUS_MASK_NONE);

    }
    
    public void stop() {
        reader.delete_contained_entities();
        subscriber.delete_datareader(reader);
        participant.delete_subscriber(subscriber);
        participant.delete_topic(topic);
        ice.NumericTypeSupport.unregister_type(participant, ice.NumericTypeSupport.get_type_name());
        DomainParticipantFactory.get_instance().delete_participant(participant);

        session.close();
    }

    
    private final ice.NumericSeq numericSequence = new ice.NumericSeq();
    private final SampleInfoSeq  sampleInfoSequence = new SampleInfoSeq();
    
    Cache<String, Numeric> instances = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    
    public void poll() {
        long start = System.nanoTime();
        Transaction t = session.beginTransaction();
        
        try {
            System.err.println("START POLL");

            reader.take(numericSequence, sampleInfoSequence, ResourceLimitsQosPolicy.LENGTH_UNLIMITED, SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE, InstanceStateKind.ANY_INSTANCE_STATE);
            final int sz = sampleInfoSequence.size();
            System.err.println("Handling " + sz + " samples");
            for(int i = 0; i < sz; i++) {
                SampleInfo si = (SampleInfo) sampleInfoSequence.get(i);
                ice.Numeric n = (ice.Numeric) numericSequence.get(i);
                
                String strInstanceHandle = b64Encoder.encodeToString(si.instance_handle.get_valuesI());
                Numeric persistedNumeric = instances.getIfPresent(strInstanceHandle);
                
                if(null == persistedNumeric) {
                    Numeric result = (Numeric) session.createQuery("from Numeric n where n.instance_handle = :instance_handle")
                            .setParameter("instance_handle", strInstanceHandle).uniqueResult();
                    if(null==result) {
                        // Create in the database
                        System.err.println("CACHE LOAD NOT SUCCESSFUL " + strInstanceHandle + " creating ");
                        persistedNumeric = new Numeric();
                        persistedNumeric.setInstance_id(n.instance_id);
                        persistedNumeric.setMetric_id(n.metric_id);
                        persistedNumeric.setUnique_device_identifier(n.unique_device_identifier);
                        persistedNumeric.setUnit_id(n.unit_id);
                        persistedNumeric.setVendor_metric_id(n.vendor_metric_id);
                        persistedNumeric.setInstance_handle(strInstanceHandle);
                        session.save(persistedNumeric);
                    } else {
                        // Found in the database
                        persistedNumeric = result;
                        System.err.println("CACHE LOAD SUCCESSFUL from DB " + n);
                    }                        

                    instances.put(strInstanceHandle, persistedNumeric);
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
            // TODO there's no special reason these should be inserted transactionally
            t.commit();
            t = null;
        } catch (RETCODE_NO_DATA noData) {
            // TODO is it better to rollback or commit an empty transaction? 
            return;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if(null != t) {
                t.rollback();
            }
            reader.return_loan(numericSequence, sampleInfoSequence);
            long elapsed = System.nanoTime()-start;
            System.err.println("END POLL took " + (elapsed/1000000000L)+"."+(elapsed%1000000000L) + " seconds");
        }
    }
}
