package org.mdpnp.oracle;

import org.mdpnp.rtiapi.data.QosProfiles;

public class AlarmLimitUtil extends AbstractIceType<ice.AlarmLimitDataReader, ice.AlarmLimit, ice.AlarmLimitSeq> {

    public AlarmLimitUtil(String topicString) {
        super(topicString, ice.AlarmLimit.class, QosProfiles.state);
    }
    

}
