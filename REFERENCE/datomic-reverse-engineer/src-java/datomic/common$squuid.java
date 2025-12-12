/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.UUID;

public final class common$squuid
extends AFunction {
    public static Object invokeStatic() {
        UUID uuid = UUID.randomUUID();
        long time = System.currentTimeMillis();
        long secs = time / 1000L;
        long lsb = uuid.getLeastSignificantBits();
        UUID uUID = uuid;
        uuid = null;
        long msb = uUID.getMostSignificantBits();
        return new UUID(secs << (int)32L | 0xFFFFFFFFL & msb, lsb);
    }

    public Object invoke() {
        return common$squuid.invokeStatic();
    }
}

