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

public final class index$repair_disjoined$fn__15707$fn__15708$fn__15712$fn__15721
extends AFunction {
    Object datoms;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");

    public index$repair_disjoined$fn__15707$fn__15708$fn__15712$fn__15721(Object object) {
        this.datoms = object;
    }

    public Object invoke(Object p1__15703_SHARP_) {
        Object object = p1__15703_SHARP_;
        p1__15703_SHARP_ = null;
        index$repair_disjoined$fn__15707$fn__15708$fn__15712$fn__15721 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.datoms);
    }
}

