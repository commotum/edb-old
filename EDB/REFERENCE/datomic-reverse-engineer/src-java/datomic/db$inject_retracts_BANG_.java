/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db$inject_retracts_BANG_$fn__13846;

public final class db$inject_retracts_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Keyword const__2 = RT.keyword(null, (String)"aevt");

    public static Object invokeStatic(Object nextp, Object db2, Object eid, Object attrid, Object local_tempids) {
        Object object = local_tempids;
        local_tempids = null;
        Object object2 = nextp;
        nextp = null;
        Object object3 = db2;
        db2 = null;
        Object object4 = attrid;
        attrid = null;
        Object object5 = eid;
        eid = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new db$inject_retracts_BANG_$fn__13846(object), object2, ((IFn)const__1.getRawRoot()).invoke(object3, (Object)const__2, (Object)Tuple.create((Object)object4, (Object)object5)));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return db$inject_retracts_BANG_.invokeStatic(object6, object7, object8, object9, object10);
    }
}

