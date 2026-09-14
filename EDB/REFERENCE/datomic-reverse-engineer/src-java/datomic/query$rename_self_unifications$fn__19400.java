/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query$rename_self_unifications$fn__19400$fn__19401;

public final class query$rename_self_unifications$fn__19400
extends AFunction {
    Object padding;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public query$rename_self_unifications$fn__19400(Object object) {
        this.padding = object;
    }

    public Object invoke(Object p1__19399_SHARP_) {
        Object object = p1__19399_SHARP_;
        p1__19399_SHARP_ = null;
        query$rename_self_unifications$fn__19400 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new query$rename_self_unifications$fn__19400$fn__19401(this_.padding), (Object)PersistentVector.EMPTY, object);
    }
}

