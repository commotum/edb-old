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
import datomic.cassandra_values_v4$put_value$fn__10290$fn__10291$fn__10295$fn__10296;

public final class cassandra_values_v4$put_value$fn__10290$fn__10291$fn__10295
extends AFunction {
    Object session;
    Object val_map;
    Object table;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.cassandra-values-v4", (String)"*retry*");
    public static final Keyword const__2 = RT.keyword(null, (String)"threw");

    public cassandra_values_v4$put_value$fn__10290$fn__10291$fn__10295(Object object, Object object2, Object object3) {
        this.session = object;
        this.val_map = object2;
        this.table = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.session = null;
            this.val_map = null;
            this.table = null;
            objectArray[1] = ((IFn)const__1.get()).invoke((Object)new cassandra_values_v4$put_value$fn__10290$fn__10291$fn__10295$fn__10296(this.session, this.val_map, this.table));
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__2;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

