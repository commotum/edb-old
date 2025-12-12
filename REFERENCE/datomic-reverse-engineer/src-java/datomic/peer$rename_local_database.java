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
import datomic.peer$rename_local_database$fn__21622;

public final class peer$rename_local_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__1 = RT.var((String)"datomic.peer", (String)"local-dbs");

    public static Object invokeStatic(Object dbname, Object newname) {
        Object object = dbname;
        dbname = null;
        Object object2 = newname;
        newname = null;
        ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), (Object)new peer$rename_local_database$fn__21622(object, object2));
        return Boolean.TRUE;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return peer$rename_local_database.invokeStatic(object3, object4);
    }
}

