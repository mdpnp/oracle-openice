package org.mdpnp.oracle;

import org.mdpnp.rtiapi.data.QosProfiles;

public class AlertUtil extends AbstractIceType<ice.AlertDataReader, ice.Alert, ice.AlertSeq> {
    public AlertUtil(final String topicString) {
        super(topicString, ice.Alert.class, QosProfiles.state);
    }
    

}
