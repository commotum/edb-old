/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class query$rae$fn__19138
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__1 = RT.var((String)"datomic.query", (String)"emap");

    public query$rae$fn__19138(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__19133_SHARP_, Object p2__19134_SHARP_) {
        Object object = p1__19133_SHARP_;
        p1__19133_SHARP_ = null;
        Object object2 = p2__19134_SHARP_;
        p2__19134_SHARP_ = null;
        query$rae$fn__19138 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke(this_.db, (Object)Numbers.num((long)((IDatum)object2).getE())));
    }
}

