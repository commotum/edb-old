/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;

public final class datalog$fn__18233$fn__18240
extends AFunction {
    Object join_map;

    public datalog$fn__18233$fn__18240(Object object) {
        this.join_map = object;
    }

    public Object invoke(Object p1__18217_SHARP_) {
        Object object = p1__18217_SHARP_;
        p1__18217_SHARP_ = null;
        datalog$fn__18233$fn__18240 this_ = null;
        return RT.get((Object)this_.join_map, (Object)object);
    }
}

