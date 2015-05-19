package org.mdpnp.hiberdds.testapp;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.mdpnp.devices.IceQos;
import org.mdpnp.rtiapi.data.QosProfiles;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.SubscriberQos;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.topic.Topic;

public class DDSWriterJDBC {
    
    
    public static void main(String[] args) throws IOException, InterruptedException, SQLException {
        IceQos.loadAndSetIceQos();
        DDSWriterJDBC writer = new DDSWriterJDBC();
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
    
    private DomainParticipant participant;
    private Topic topic;
    private Subscriber subscriber;
    private ice.NumericDataReader reader;
    private Connection conn;
    private final NumericUtil numericUtil = new NumericUtil();
    
    public void start() throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.7.25:1521/XE", "openice2", "openice2");
        conn.setAutoCommit(false);
        
        numericUtil.prepare(conn);
        
        participant = DomainParticipantFactory.get_instance().create_participant(15, 
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
    
    public void stop() throws SQLException {
        reader.delete_contained_entities();
        subscriber.delete_datareader(reader);
        participant.delete_subscriber(subscriber);
        participant.delete_topic(topic);
        ice.NumericTypeSupport.unregister_type(participant, ice.NumericTypeSupport.get_type_name());
        DomainParticipantFactory.get_instance().delete_participant(participant);

        numericUtil.close();
        conn.close();
    }

    
    private final ice.NumericSeq numericSequence = new ice.NumericSeq();
    private final SampleInfoSeq  sampleInfoSequence = new SampleInfoSeq();
    

    
    public void poll() {
        long start = System.nanoTime();
        int size = 0;

        try {
            reader.take(numericSequence, sampleInfoSequence, ResourceLimitsQosPolicy.LENGTH_UNLIMITED, SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE, InstanceStateKind.ANY_INSTANCE_STATE);
            final int sz = size = sampleInfoSequence.size();
            System.err.println("POLL "+sz+" samples");
            numericUtil.batchUpdate(reader, numericSequence, sampleInfoSequence);
            conn.commit();
        } catch (RETCODE_NO_DATA noData) {
            // TODO is it better to rollback or commit an empty transaction? 
            return;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            reader.return_loan(numericSequence, sampleInfoSequence);
            long elapsed = System.nanoTime()-start;
            long elapsedMS = elapsed / 1000000L;
            System.err.println("END POLL took " + (elapsed/1000000000L)+"s "+(elapsed%1000000000L) + "ns "+(0==size?"NA":(""+(1.0*elapsedMS/size))) + "ms/sample");
        }
    }
}
