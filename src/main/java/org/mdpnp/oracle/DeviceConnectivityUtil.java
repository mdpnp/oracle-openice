package org.mdpnp.oracle;

import org.mdpnp.rtiapi.data.QosProfiles;

public class DeviceConnectivityUtil extends AbstractIceType<ice.DeviceConnectivityDataReader, ice.DeviceConnectivity, ice.DeviceConnectivitySeq> {

    public DeviceConnectivityUtil(String topicString) {
        super(topicString, ice.DeviceConnectivity.class, QosProfiles.state);
    }

}
