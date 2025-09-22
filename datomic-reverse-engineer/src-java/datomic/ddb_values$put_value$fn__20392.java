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

public final class ddb_values$put_value$fn__20392
extends AFunction {
    int n;
    Object chunks;
    Object id;
    Object table;
    Object ddb_client;
    Object base;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.ddb", (String)"put-item");
    public static final Keyword const__2 = RT.keyword(null, (String)"tableName");
    public static final Keyword const__3 = RT.keyword(null, (String)"item");
    public static final Var const__4 = RT.var((String)"datomic.ddb", (String)"create-item");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__6 = RT.keyword(null, (String)"id");
    public static final Keyword const__7 = RT.keyword(null, (String)"v");
    public static final Keyword const__10 = RT.keyword(null, (String)"__n");
    public static final Keyword const__11 = RT.keyword(null, (String)"threw");

    public ddb_values$put_value$fn__20392(int n, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.n = n;
        this.chunks = object;
        this.id = object2;
        this.table = object3;
        this.ddb_client = object4;
        this.base = object5;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.ddb_client = null;
            Object[] objectArray2 = new Object[4];
            objectArray2[0] = const__2;
            objectArray2[1] = this.table = null;
            objectArray2[2] = const__3;
            this.base = null;
            this.id = null;
            this.chunks = null;
            objectArray2[3] = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(this.base, (Object)const__6, this.id, (Object)const__7, RT.nth((Object)this.chunks, (int)RT.intCast((long)0L)), (Object)const__10, (Object)this.n));
            objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(this.ddb_client, (Object)RT.mapUniqueKeys((Object[])objectArray2));
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__11;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

