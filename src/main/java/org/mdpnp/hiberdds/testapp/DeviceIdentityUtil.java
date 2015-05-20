package org.mdpnp.hiberdds.testapp;

import org.mdpnp.rtiapi.data.QosProfiles;

public class DeviceIdentityUtil extends AbstractIceType<ice.DeviceIdentityDataReader, ice.DeviceIdentity, ice.DeviceIdentitySeq> {

    public DeviceIdentityUtil(String topicString) {
        super(topicString, ice.DeviceIdentity.class, QosProfiles.device_identity);
    }
    
    
}
