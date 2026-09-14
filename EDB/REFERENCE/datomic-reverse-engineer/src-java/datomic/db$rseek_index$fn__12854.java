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
import java.util.Comparator;

public final class db$rseek_index$fn__12854
extends AFunction {
    Object d;
    Object cmp;

    public db$rseek_index$fn__12854(Object object, Object object2) {
        this.d = object;
        this.cmp = object2;
    }

    public Object invoke(Object p1__12848_SHARP_) {
        Object object = p1__12848_SHARP_;
        p1__12848_SHARP_ = null;
        db$rseek_index$fn__12854 this_ = null;
        return Numbers.isNeg((long)((Comparator)this_.cmp).compare(this_.d, object)) ? Boolean.TRUE : Boolean.FALSE;
    }
}

