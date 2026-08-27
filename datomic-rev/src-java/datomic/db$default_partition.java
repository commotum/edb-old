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
import datomic.db.Db;

public final class db$default_partition
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"explicit-partition");
    public static final Object const__2 = 4L;

    public static Object invokeStatic(Object db2) {
        Object object;
        Object or__5238__auto__13868;
        Object object2;
        Object temp__5457__auto__13867;
        Object object3 = temp__5457__auto__13867 = ((Db)db2).entid(((IFn)const__0.getRawRoot()).invoke((Object)"datomic.defaultPartition"));
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5457__auto__13867;
            temp__5457__auto__13867 = null;
            Object p = object4;
            Object object5 = db2;
            db2 = null;
            Object object6 = ((IFn)const__1.getRawRoot()).invoke(object5, p);
            if (object6 != null && object6 != Boolean.FALSE) {
                object2 = p;
                p = null;
            } else {
                object2 = null;
            }
        } else {
            object2 = null;
        }
        Object object7 = or__5238__auto__13868 = object2;
        if (object7 != null && object7 != Boolean.FALSE) {
            object = or__5238__auto__13868;
            or__5238__auto__13868 = null;
        } else {
            object = const__2;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$default_partition.invokeStatic(object2);
    }
}

