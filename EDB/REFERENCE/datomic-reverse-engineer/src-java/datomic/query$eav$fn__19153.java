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
import datomic.impl.db.IDatum;

public final class query$eav$fn__19153
extends AFunction {
    Object maybe_bind;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"conj");

    public query$eav$fn__19153(Object object) {
        this.maybe_bind = object;
    }

    public Object invoke(Object p1__19146_SHARP_, Object p2__19147_SHARP_) {
        Object object = p1__19146_SHARP_;
        p1__19146_SHARP_ = null;
        Object object2 = p2__19147_SHARP_;
        p2__19147_SHARP_ = null;
        query$eav$fn__19153 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)this_.maybe_bind).invoke(((IDatum)object2).getV()));
    }
}

