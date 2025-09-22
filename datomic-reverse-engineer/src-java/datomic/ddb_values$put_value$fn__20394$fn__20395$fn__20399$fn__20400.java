/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ddb_values$put_value$fn__20394$fn__20395$fn__20399$fn__20400
extends AFunction {
    Object n;
    Object chunks;
    Object id;
    Object table;
    Object ddb_client;
    public static final Var const__0 = RT.var((String)"datomic.ddb", (String)"put-item");
    public static final Keyword const__1 = RT.keyword(null, (String)"tableName");
    public static final Keyword const__2 = RT.keyword(null, (String)"item");
    public static final Var const__3 = RT.var((String)"datomic.ddb", (String)"create-item");
    public static final Keyword const__4 = RT.keyword(null, (String)"id");
    public static final Var const__5 = RT.var((String)"datomic.ddb-values", (String)"chunk-key");
    public static final Keyword const__6 = RT.keyword(null, (String)"v");

    public ddb_values$put_value$fn__20394$fn__20395$fn__20399$fn__20400(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.n = object;
        this.chunks = object2;
        this.id = object3;
        this.table = object4;
        this.ddb_client = object5;
    }

    public Object invoke() {
        ddb_values$put_value$fn__20394$fn__20395$fn__20399$fn__20400 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.ddb_client, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, this_.table, const__2, ((IFn)const__3.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__4, ((IFn)const__5.getRawRoot()).invoke(this_.id, this_.n), const__6, RT.nth((Object)this_.chunks, (int)RT.intCast((Object)((Number)this_.n)))}))}));
    }
}

