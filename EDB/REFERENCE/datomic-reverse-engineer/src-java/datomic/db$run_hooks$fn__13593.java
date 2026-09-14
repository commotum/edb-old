/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Tuple
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IPersistentVector;
import clojure.lang.Tuple;

public final class db$run_hooks$fn__13593
extends AFunction {
    public Object invoke(Object p1__13582_SHARP_, Object p2__13581_SHARP_) {
        IPersistentVector iPersistentVector;
        Object object = p2__13581_SHARP_;
        if (object != null && object != Boolean.FALSE) {
            Object object2 = p1__13582_SHARP_;
            p1__13582_SHARP_ = null;
            Object object3 = p2__13581_SHARP_;
            p2__13581_SHARP_ = null;
            iPersistentVector = Tuple.create((Object)object2, (Object)object3);
        } else {
            iPersistentVector = null;
        }
        return iPersistentVector;
    }
}

