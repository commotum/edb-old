/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.nio.ByteBuffer;

public final class cassandra_values_v4$put_value$fn__10283
extends AFunction {
    public Object invoke(Object bbuf) {
        Object object = bbuf;
        bbuf = null;
        return ((ByteBuffer)object).asReadOnlyBuffer();
    }
}

