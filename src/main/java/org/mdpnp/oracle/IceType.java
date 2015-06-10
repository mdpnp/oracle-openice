package org.mdpnp.oracle;

import java.sql.Connection;
import java.sql.SQLException;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.subscription.Subscriber;

public interface IceType {
    void register(DomainParticipant participant, Subscriber subscriber);
    void prepare(Connection conn) throws SQLException;
    void close(DomainParticipant participant, Subscriber subscriber) throws SQLException;
    public int poll();
}
