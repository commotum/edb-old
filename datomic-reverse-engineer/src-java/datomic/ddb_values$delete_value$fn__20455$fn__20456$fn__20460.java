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

public final class ddb_values$delete_value$fn__20455$fn__20456$fn__20460
extends AFunction {
    Object ddb_client;
    Object id;
    Object p1__20451_SHARP_;
    Object table;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.ddb", (String)"delete-item");
    public static final Keyword const__2 = RT.keyword(null, (String)"tableName");
    public static final Keyword const__3 = RT.keyword(null, (String)"key");
    public static final Var const__4 = RT.var((String)"datomic.ddb", (String)"create-key");
    public static final Var const__5 = RT.var((String)"datomic.ddb-values", (String)"chunk-key");
    public static final Keyword const__6 = RT.keyword(null, (String)"returnValues");
    public static final Keyword const__7 = RT.keyword(null, (String)"threw");

    public ddb_values$delete_value$fn__20455$fn__20456$fn__20460(Object object, Object object2, Object object3, Object object4) {
        this.ddb_client = object;
        this.id = object2;
        this.p1__20451_SHARP_ = object3;
        this.table = object4;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object[] objectArray2 = new Object[6];
            objectArray2[0] = const__2;
            objectArray2[1] = this.table;
            objectArray2[2] = const__3;
            this.p1__20451_SHARP_ = null;
            objectArray2[3] = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(this.id, this.p1__20451_SHARP_));
            objectArray2[4] = const__6;
            objectArray2[5] = "NONE";
            objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(this.ddb_client, (Object)RT.mapUniqueKeys((Object[])objectArray2));
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__7;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

