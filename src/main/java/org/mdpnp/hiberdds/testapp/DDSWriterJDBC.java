package org.mdpnp.hiberdds.testapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.mdpnp.devices.IceQos;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.SubscriberQos;


public class DDSWriterJDBC {

    private static final long INTERVAL_MS = 10000L;
    
    public static void main(String[] args) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        IceQos.loadAndSetIceQos();
        DDSWriterJDBC writer = new DDSWriterJDBC();
        
        {
        
            Properties props = new Properties();
            File config = new File(".config");
            
            int domainId = 15;
            String dbUrl = "jdbc:oracle:thin:@192.168.7.25:1521/XE";
            String dbUser = "openice2";
            String dbPass = "openice2";
            
            if(config.canRead()) {
                props.load(new FileInputStream(config));
                domainId = Integer.parseInt(props.getProperty("domain", Integer.toString(domainId)));
                dbUrl    = props.getProperty("url", dbUrl);
                dbUser   = props.getProperty("user", dbUser);
                dbPass   = props.getProperty("pass", dbPass);
            }
            
            domainId = args.length > 0 ? Integer.parseInt(args[0]) : 15;
            dbUrl = args.length > 1 ? args[1] : "jdbc:oracle:thin:@192.168.7.25:1521/XE";
            dbUser = args.length > 2 ? args[2] : "openice2";
            dbPass = args.length > 3 ? args[3] : "openice2";
            writer.start(domainId, dbUrl, dbUser, dbPass);
        }
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> task = executor.scheduleAtFixedRate(() -> writer.poll(), INTERVAL_MS - System.currentTimeMillis() % INTERVAL_MS, INTERVAL_MS,
                TimeUnit.MILLISECONDS);
        System.err.println("Press enter to end the program");
        System.in.read();
        task.cancel(false);
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        writer.stop();
        System.exit(0);
    }

    private DomainParticipant participant;

    private Subscriber subscriber;

    private Connection conn;
    
    private final IceType[] iceTypes = new IceType[] {
      new NumericUtil(ice.NumericTopic.VALUE),
      new AlertUtil(ice.PatientAlertTopic.VALUE),
      new AlertUtil(ice.TechnicalAlertTopic.VALUE),
      new DeviceConnectivityUtil(ice.DeviceConnectivityTopic.VALUE),
      new DeviceIdentityUtil(ice.DeviceIdentityTopic.VALUE),
      new AlarmLimitUtil(ice.AlarmLimitTopic.VALUE)
    };

    public void start(int domainId, String dbUrl, String username, String password) throws SQLException, ClassNotFoundException {
//        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        Class.forName("oracle.jdbc.driver.OracleDriver");
        conn = DriverManager.getConnection(dbUrl, username, password);
        conn.setAutoCommit(false);

        for(IceType it : iceTypes) {
            it.prepare(conn);
        }
        
        participant = DomainParticipantFactory.get_instance().create_participant(domainId, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, null,
                StatusKind.STATUS_MASK_NONE);

        subscriber = participant.create_subscriber(DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null, StatusKind.STATUS_MASK_NONE);

        SubscriberQos sQos = new SubscriberQos();
        subscriber.get_qos(sQos);
        sQos.partition.name.add("*");
        subscriber.set_qos(sQos);

        for(IceType it : iceTypes) {
            it.register(participant, subscriber);
        }

    }

    public void stop() throws SQLException {
        for(IceType it : iceTypes) {
            it.close(participant, subscriber);
        }

        participant.delete_subscriber(subscriber);

        DomainParticipantFactory.get_instance().delete_participant(participant);

        conn.close();
    }

    private final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    public void poll() {
        String startTime = sdf.format(new Date());
        long start = System.nanoTime();
        int size = 0;

        for(IceType it : iceTypes) {
            size += it.poll();
        }

        long elapsed = System.nanoTime() - start;
        long elapsedMS = elapsed / 1000000L;
        System.err.println(startTime+" END POLL " + size + " samples took " + (elapsed / 1000000000L) + "s " + (elapsed % 1000000000L) + "ns "
                + (0 == size ? "NA" : ("" + (1.0 * elapsedMS / size))) + "ms/sample");
    }
}
