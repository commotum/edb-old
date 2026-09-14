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
import datomic.peer.LocalConnection;

public final class peer$delete_local_database$fn__21618
extends AFunction {
    Object dbname;
    Object deleted;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deliver");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"dissoc");

    public peer$delete_local_database$fn__21618(Object object, Object object2) {
        this.dbname = object;
        this.deleted = object2;
    }

    public Object invoke(Object dbs) {
        Object object;
        Object temp__5455__auto__21620;
        Object object2 = temp__5455__auto__21620 = RT.get((Object)dbs, (Object)this_.dbname);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object conn;
            Object object3 = temp__5455__auto__21620;
            temp__5455__auto__21620 = null;
            Object object4 = conn = object3;
            conn = null;
            ((IFn)const__1.getRawRoot()).invoke(((LocalConnection)object4).released, (Object)Boolean.TRUE);
            ((IFn)const__2.getRawRoot()).invoke(this_.deleted, (Object)Boolean.TRUE);
            Object object5 = dbs;
            dbs = null;
            peer$delete_local_database$fn__21618 this_ = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object5, this_.dbname);
        } else {
            object = dbs;
            Object var1_1 = null;
        }
        return object;
    }
}

