/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.EAVof;
import java.util.HashMap;

public final class db$create_op_validator$fn__13301
extends AFunction {
    Object eavmap;
    Object db;
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"datoms-conflict");

    public db$create_op_validator$fn__13301(Object object, Object object2) {
        this.eavmap = object;
        this.db = object2;
    }

    public Object invoke(Object d) {
        Object object;
        Object v = ((HashMap)this_.eavmap).put(new EAVof(d), d);
        boolean or__5238__auto__13303 = Util.identical((Object)v, null);
        if (or__5238__auto__13303) {
            object = or__5238__auto__13303 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            Object object2 = v;
            v = null;
            Object object3 = d;
            d = null;
            db$create_op_validator$fn__13301 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(this_.db, object2, object3);
        }
        return object;
    }
}

