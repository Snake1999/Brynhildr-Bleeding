package io.nukkit.block;

import io.nukkit.util.Identifiers;

/**
 * Created by Mulan Lin('Snake1999') on 2016/12/3 17:43.
 * All rights reserved
 */
public interface BlockIdentifier {

    BlockIdentifier IDENTIFIER_AIR = Identifiers.BLOCK_AIR;

    int asIntegerId();

    int asIntegerMeta();

    default BlockIdentifier withIntegerMeta(int meta) {
        return Identifiers.withIntegerMeta(this, meta);
    }

}
