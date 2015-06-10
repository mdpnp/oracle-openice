package org.mdpnp.oracle;

import org.mdpnp.rtiapi.data.QosProfiles;

public class DeviceIdentityUtil extends AbstractIceType<ice.DeviceIdentityDataReader, ice.DeviceIdentity, ice.DeviceIdentitySeq> {

    public DeviceIdentityUtil(String topicString) {
        super(topicString, ice.DeviceIdentity.class, QosProfiles.device_identity);
    }
    
    
}
