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

public final class backup$backup
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.backup", (String)"create-storage");
    public static final Var const__1 = RT.var((String)"datomic.backup", (String)"backup-db");
    public static final Var const__2 = RT.var((String)"datomic.backup", (String)"backup-concurrency");

    public static Object invokeStatic(Object from_conn_uri, Object to_storage_uri, Object sse_QMARK_, Object progress, Object incremental_QMARK_) {
        Object object = sse_QMARK_;
        sse_QMARK_ = null;
        Object storage = ((IFn)const__0.getRawRoot()).invoke(to_storage_uri, object);
        Object object2 = from_conn_uri;
        from_conn_uri = null;
        Object object3 = storage;
        storage = null;
        Object object4 = progress;
        progress = null;
        Object object5 = to_storage_uri;
        to_storage_uri = null;
        Object object6 = incremental_QMARK_;
        incremental_QMARK_ = null;
        return ((IFn)const__1.getRawRoot()).invoke(object2, object3, object4, ((IFn)const__2.getRawRoot()).invoke(object5), object6);
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
        return backup$backup.invokeStatic(object6, object7, object8, object9, object10);
    }
}

