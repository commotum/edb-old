/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.ArrayList;

public final class index$get_avet_sorted_datoms_mem$fn__15531
extends AFunction {
    Object ret;

    public index$get_avet_sorted_datoms_mem$fn__15531(Object object) {
        this.ret = object;
    }

    public Object invoke(Object p1__15521_SHARP_, Object p2__15520_SHARP_) {
        Object object = p2__15520_SHARP_;
        p2__15520_SHARP_ = null;
        return ((ArrayList)this.ret).add(object) ? Boolean.TRUE : Boolean.FALSE;
    }
}

