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
import datomic.db.Attribute;

public final class integrity$aevt_avet_stats$fn__22483
extends AFunction {
    Object nohists;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");

    public integrity$aevt_avet_stats$fn__22483(Object object) {
        this.nohists = object;
    }

    public Object invoke(Object p1__22479_SHARP_) {
        Object object = p1__22479_SHARP_;
        p1__22479_SHARP_ = null;
        integrity$aevt_avet_stats$fn__22483 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.nohists, ((Attribute)object).id());
    }
}

