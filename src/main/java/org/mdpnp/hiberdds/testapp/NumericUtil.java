package org.mdpnp.hiberdds.testapp;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;

public class NumericUtil {
    private CallableStatement insertUpdateNumeric;
    private PreparedStatement insertNumericSample;
    Cache<InstanceHandle_t, Long> instances = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    
    
    public void prepare(final Connection conn) throws SQLException {
        insertNumericSample = conn.prepareStatement("INSERT INTO NUMERIC_SAMPLE (NUMERIC_SAMPLE_ID, NUMERIC_ID, SOURCE_TIME, VALUE) VALUES (NUMERIC_SAMPLE_SEQ.NEXTVAL, ?, ?, ?)");
        insertUpdateNumeric = conn.prepareCall("{? = call INSERT_UPDATE_NUMERIC (?, ?, ?, ?, ?)}");
        insertUpdateNumeric.registerOutParameter(1, java.sql.Types.INTEGER);
    }
    
    public void batchUpdate(final ice.NumericDataReader reader, final ice.NumericSeq numericSequence, final SampleInfoSeq sampleInfoSequence) throws SQLException {
        insertNumericSample.clearBatch();
        final int sz = sampleInfoSequence.size();
        for(int i = 0; i < sz; i++) {
            
            SampleInfo si = (SampleInfo) sampleInfoSequence.get(i);
            ice.Numeric n = (ice.Numeric) numericSequence.get(i);
            
            Long persistedNumeric = instances.getIfPresent(si.instance_handle);
            
            if(!si.valid_data) {
                reader.get_key_value(n, si.instance_handle);
            }
            
            if(null == persistedNumeric) {
                insertUpdateNumeric.clearParameters();
                insertUpdateNumeric.setString(2, n.unique_device_identifier);
                insertUpdateNumeric.setString(3, n.metric_id);
                insertUpdateNumeric.setString(4, n.vendor_metric_id);
                insertUpdateNumeric.setInt(5, n.instance_id);
                insertUpdateNumeric.setString(6, n.unit_id);
                insertUpdateNumeric.execute();

                persistedNumeric = insertUpdateNumeric.getLong(1);
                
                System.err.println("From DB this instance is " + persistedNumeric);
                instances.put(new InstanceHandle_t(si.instance_handle), persistedNumeric);
            }
            
            if(si.valid_data) {
                insertNumericSample.clearParameters();
                insertNumericSample.setLong(1, persistedNumeric);
//                insertNumericSample.setTimestamp(2, new java.sql.Timestamp(n.device_time.sec * 1000L + n.device_time.nanosec / 1000000L));
//                insertNumericSample.setTimestamp(3, new java.sql.Timestamp(n.presentation_time.sec * 1000L + n.presentation_time.nanosec / 1000000L));
                insertNumericSample.setTimestamp(2, new java.sql.Timestamp(si.source_timestamp.sec * 1000L + si.source_timestamp.nanosec / 1000000L));
                // TODO this should be a floating point number
                insertNumericSample.setFloat(3, n.value);
                insertNumericSample.addBatch();
            }
        }
        insertNumericSample.executeBatch();

    }
    
    public void close() throws SQLException {
        insertUpdateNumeric.close();
        insertNumericSample.close();
    }
}
