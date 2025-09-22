/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.peer.LocalConnection;

public final class peer$local_connection
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"bootstrap-db");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"promise");
    public static final Var const__3 = RT.var((String)"datomic.peer", (String)"create-t-watcher");
    public static final Keyword const__4 = RT.keyword(null, (String)"basisT");

    public static Object invokeStatic(Object dbname) {
        Object db2;
        Object object = db2 = ((IFn)const__0.getRawRoot()).invoke();
        db2 = null;
        Object db_ref = ((IFn)const__1.getRawRoot()).invoke(object);
        Object object2 = dbname;
        dbname = null;
        Object object3 = db_ref;
        Object object4 = db_ref;
        db_ref = null;
        return new LocalConnection(object2, object3, ((IFn)const__1.getRawRoot()).invoke(null), ((IFn)const__2.getRawRoot()).invoke(), ((IFn)const__3.getRawRoot()).invoke((Object)const__4, object4));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$local_connection.invokeStatic(object2);
    }
}

