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
import datomic.impl.db.IDatum;

public final class index$merge_one_index$fn__15507
extends AFunction {
    Object as_of_t;

    public index$merge_one_index$fn__15507(Object object) {
        this.as_of_t = object;
    }

    public Object invoke(Object p1__15444_SHARP_) {
        Object object = p1__15444_SHARP_;
        p1__15444_SHARP_ = null;
        index$merge_one_index$fn__15507 this_ = null;
        return Numbers.lt((long)((IDatum)object).getT(), (Object)this_.as_of_t) ? Boolean.TRUE : Boolean.FALSE;
    }
}

