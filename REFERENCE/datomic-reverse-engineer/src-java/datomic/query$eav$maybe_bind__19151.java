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

public final class query$eav$maybe_bind__19151
extends AFunction {
    Object ref_QMARK_;
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.query", (String)"ref-val");

    public query$eav$maybe_bind__19151(Object object, Object object2) {
        this.ref_QMARK_ = object;
        this.db = object2;
    }

    public Object invoke(Object v) {
        Object object = v;
        v = null;
        query$eav$maybe_bind__19151 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, this_.ref_QMARK_, object);
    }
}

