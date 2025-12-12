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

public final class peer$create_local_database$fn__21615
extends AFunction {
    Object dbname;
    Object created;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__3 = RT.var((String)"datomic.peer", (String)"local-connection");

    public peer$create_local_database$fn__21615(Object object, Object object2) {
        this.dbname = object;
        this.created = object2;
    }

    public Object invoke(Object dbs) {
        Object object;
        Object object2 = RT.get((Object)dbs, (Object)this_.dbname);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = dbs;
            dbs = null;
        } else {
            ((IFn)const__1.getRawRoot()).invoke(this_.created, (Object)Boolean.TRUE);
            Object object3 = dbs;
            dbs = null;
            peer$create_local_database$fn__21615 this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object3, this_.dbname, ((IFn)const__3.getRawRoot()).invoke(this_.dbname));
        }
        return object;
    }
}

