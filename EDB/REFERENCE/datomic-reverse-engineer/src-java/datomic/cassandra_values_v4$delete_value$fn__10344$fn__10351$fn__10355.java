/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class cassandra_values_v4$delete_value$fn__10344$fn__10351$fn__10355
extends AFunction {
    Object p1__10340_SHARP_;
    Object id;
    Object table;
    Object session;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.cassandra-v4", (String)"cql-delete");
    public static final Var const__2 = RT.var((String)"datomic.cassandra-values-v4", (String)"chunk-key");
    public static final Var const__3 = RT.var((String)"datomic.cassandra-values-v4", (String)"cql-keys");
    public static final Keyword const__4 = RT.keyword(null, (String)"threw");

    public cassandra_values_v4$delete_value$fn__10344$fn__10351$fn__10355(Object object, Object object2, Object object3, Object object4) {
        this.p1__10340_SHARP_ = object;
        this.id = object2;
        this.table = object3;
        this.session = object4;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.p1__10340_SHARP_ = null;
            objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(this.session, this.table, ((IFn)const__2.getRawRoot()).invoke(this.id, this.p1__10340_SHARP_), const__3.getRawRoot());
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

