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

public final class index$merge_db_STAR_$build_tiered_index__15765$fn__15815$fn__15825
extends AFunction {
    Object olookup;
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"getx-uncached");

    public index$merge_db_STAR_$build_tiered_index__15765$fn__15815$fn__15825(Object object) {
        this.olookup = object;
    }

    public Object invoke(Object p1__15736_SHARP_) {
        Object object = p1__15736_SHARP_;
        p1__15736_SHARP_ = null;
        index$merge_db_STAR_$build_tiered_index__15765$fn__15815$fn__15825 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.olookup, object);
    }
}

