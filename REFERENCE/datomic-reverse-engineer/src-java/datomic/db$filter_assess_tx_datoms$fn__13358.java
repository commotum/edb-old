/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class db$filter_assess_tx_datoms$fn__13358
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"first");

    public db$filter_assess_tx_datoms$fn__13358(Object object) {
        this.db = object;
    }

    public Object invoke(Object result2, Object aid) {
        Object object = ((IFn)const__0.getRawRoot()).invoke(this_.db, aid);
        long hook_attr = object != null && object != Boolean.FALSE ? 19L : 13L;
        Object object2 = result2;
        Object object3 = aid;
        aid = null;
        Object object4 = result2;
        result2 = null;
        db$filter_assess_tx_datoms$fn__13358 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2, ((IFn.LLOLO)const__4.getRawRoot()).invokePrim(0L, hook_attr, object3, ((IDatum)((IFn)const__6.getRawRoot()).invoke(object4)).getT()));
    }
}

