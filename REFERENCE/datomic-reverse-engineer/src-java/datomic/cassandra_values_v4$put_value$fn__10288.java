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

public final class cassandra_values_v4$put_value$fn__10288
extends AFunction {
    Object val_map;
    Object session;
    Object table;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.cassandra-v4", (String)"cql-insert");
    public static final Var const__2 = RT.var((String)"datomic.cassandra-values-v4", (String)"cql-keys");
    public static final Keyword const__3 = RT.keyword(null, (String)"threw");

    public cassandra_values_v4$put_value$fn__10288(Object object, Object object2, Object object3) {
        this.val_map = object;
        this.session = object2;
        this.table = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.session = null;
            this.table = null;
            this.val_map = null;
            objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(this.session, this.table, const__2.getRawRoot(), this.val_map, (Object)Boolean.FALSE);
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

