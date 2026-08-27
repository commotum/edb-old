/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$least_pop_slice$fn__15647
extends AFunction {
    Object index;
    Object dir_partition_size;
    Object olookup;
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"estimate-seg-offset");

    public index$least_pop_slice$fn__15647(Object object, Object object2, Object object3) {
        this.index = object;
        this.dir_partition_size = object2;
        this.olookup = object3;
    }

    public Object invoke(Object p1__15646_SHARP_) {
        Object object = p1__15646_SHARP_;
        p1__15646_SHARP_ = null;
        index$least_pop_slice$fn__15647 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.olookup, this_.index, this_.dir_partition_size, object);
    }
}

