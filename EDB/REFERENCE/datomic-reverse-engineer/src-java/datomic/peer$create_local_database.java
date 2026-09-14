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
import datomic.peer$create_local_database$fn__21615;

public final class peer$create_local_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__2 = RT.var((String)"datomic.peer", (String)"local-dbs");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"deref");

    public static Object invokeStatic(Object dbname, Object uri2) {
        Object created = ((IFn)const__0.getRawRoot()).invoke((Object)Boolean.FALSE);
        Object object = dbname;
        dbname = null;
        ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), (Object)new peer$create_local_database$fn__21615(object, created));
        Object object2 = created;
        created = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return peer$create_local_database.invokeStatic(object3, object4);
    }
}

