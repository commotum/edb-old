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

public final class cassandra_values$delete_value$fn__20586$fn__20590
extends AFunction {
    Object id;
    Object table;
    Object session;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.cassandra", (String)"cql-select");
    public static final Var const__2 = RT.var((String)"datomic.cassandra-values", (String)"cql-keys");
    public static final Keyword const__3 = RT.keyword(null, (String)"threw");

    public cassandra_values$delete_value$fn__20586$fn__20590(Object object, Object object2, Object object3) {
        this.id = object;
        this.table = object2;
        this.session = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.session = null;
            this.table = null;
            this.id = null;
            objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(this.session, this.table, this.id, const__2.getRawRoot(), (Object)Boolean.FALSE);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__3;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

