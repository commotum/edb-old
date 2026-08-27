/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;

public final class process_monitor$convert_nanos_to_millis$round__23488
extends AFunction {
    public Object invoke(Object nanos) {
        Object object = nanos;
        nanos = null;
        process_monitor$convert_nanos_to_millis$round__23488 this_ = null;
        return Numbers.divide((Object)Numbers.quotient((Object)object, (long)10000L), (double)100.0);
    }
}

