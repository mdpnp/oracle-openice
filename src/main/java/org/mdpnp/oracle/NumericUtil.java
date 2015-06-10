package org.mdpnp.oracle;

import org.mdpnp.rtiapi.data.QosProfiles;

public class NumericUtil extends AbstractIceType<ice.NumericDataReader, ice.Numeric, ice.NumericSeq> {

    public NumericUtil(final String topicString) {
        super(topicString, ice.Numeric.class, QosProfiles.numeric_data);
    }
}
