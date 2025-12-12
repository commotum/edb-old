/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.ddb_values$put_value$fn__20394$fn__20395;
import datomic.future.GetChannel;

public final class ddb_values$put_value$fn__20394
extends AFunction {
    Object chunks;
    Object id;
    Object table;
    Object ddb_client;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final AFn const__11;
    public static final Var const__12;

    public ddb_values$put_value$fn__20394(Object object, Object object2, Object object3, Object object4) {
        this.chunks = object;
        this.id = object2;
        this.table = object3;
        this.ddb_client = object4;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object rets, Object n) {
        v0 = (IFn)ddb_values$put_value$fn__20394.const__0.getRawRoot();
        v1 = rets;
        rets = null;
        v2 = n;
        n = null;
        f__10267__auto__20409 = ((IFn)ddb_values$put_value$fn__20394.const__1.getRawRoot()).invoke(((IFn)ddb_values$put_value$fn__20394.const__2.getRawRoot()).invoke(ddb_values$put_value$fn__20394.const__3.getRawRoot()), (Object)new ddb_values$put_value$fn__20394$fn__20395(v2, this.chunks, this.id, this.table, this.ddb_client));
        v3 = f__10267__auto__20409;
        if (Util.classOf((Object)v3) == ddb_values$put_value$fn__20394.__cached_class__0) ** GOTO lbl12
        if (!(v3 instanceof GetChannel)) {
            v3 = v3;
            ddb_values$put_value$fn__20394.__cached_class__0 = Util.classOf((Object)v3);
lbl12:
            // 2 sources

            v4 = ddb_values$put_value$fn__20394.const__4.getRawRoot().invoke(v3);
        } else {
            v4 = ((GetChannel)v3).get_channel();
        }
        v5 = ch__10268__auto__20410 = v4;
        ch__10268__auto__20410 = null;
        ((IFn)ddb_values$put_value$fn__20394.const__5.getRawRoot()).invoke(v5, (Object)ddb_values$put_value$fn__20394.const__11, ((IFn)ddb_values$put_value$fn__20394.const__2.getRawRoot()).invoke(ddb_values$put_value$fn__20394.const__12.getRawRoot()));
        var3_3 = null;
        this = null;
        return v0.invoke(v1, f__10267__auto__20409);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"conj");
        const__1 = RT.var((String)"datomic.future", (String)"-future-with-channel-impl");
        const__2 = RT.var((String)"clojure.core", (String)"deref");
        const__3 = RT.var((String)"datomic.ddb-values", (String)"chunk-pool");
        const__4 = RT.var((String)"datomic.future", (String)"get-channel");
        const__5 = RT.var((String)"datomic.future", (String)"add-bounding-warning");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 80, RT.keyword(null, (String)"column"), 31, RT.keyword(null, (String)"file"), "datomic/ddb_values.clj"});
        const__12 = RT.var((String)"datomic.future", (String)"bounding-warn-seconds");
    }
}

