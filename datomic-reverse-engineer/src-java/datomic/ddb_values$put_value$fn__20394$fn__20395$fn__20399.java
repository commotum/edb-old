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
import datomic.ddb_values$put_value$fn__20394$fn__20395$fn__20399$fn__20400;

public final class ddb_values$put_value$fn__20394$fn__20395$fn__20399
extends AFunction {
    Object n;
    Object chunks;
    Object id;
    Object table;
    Object ddb_client;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.ddb-values", (String)"*retry*");
    public static final Keyword const__2 = RT.keyword(null, (String)"threw");

    public ddb_values$put_value$fn__20394$fn__20395$fn__20399(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.n = object;
        this.chunks = object2;
        this.id = object3;
        this.table = object4;
        this.ddb_client = object5;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.n = null;
            this.chunks = null;
            this.id = null;
            this.table = null;
            this.ddb_client = null;
            objectArray[1] = ((IFn)const__1.get()).invoke((Object)new ddb_values$put_value$fn__20394$fn__20395$fn__20399$fn__20400(this.n, this.chunks, this.id, this.table, this.ddb_client));
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

