package org.mdpnp.hiberdds.testapp;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.topic.Topic;

public abstract class AbstractIceType implements IceType {
    protected CallableStatement insertUpdateInstance;
    protected PreparedStatement insertSample;
    protected Cache<InstanceHandle_t, Long> instances = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    protected Topic topic;
}
