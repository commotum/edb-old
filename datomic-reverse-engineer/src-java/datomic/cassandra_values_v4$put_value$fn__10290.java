/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cassandra_values_v4$put_value$fn__10290$fn__10291;
import datomic.future.GetChannel;

public final class cassandra_values_v4$put_value$fn__10290
extends AFunction {
    Object session;
    Object id;
    Object chunks;
    Object table;
    private static Class __cached_class__0;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final AFn const__15;
    public static final Var const__16;

    public cassandra_values_v4$put_value$fn__10290(Object object, Object object2, Object object3, Object object4) {
        this.session = object;
        this.id = object2;
        this.chunks = object3;
        this.table = object4;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object rets, Object n) {
        val_map = RT.mapUniqueKeys((Object[])new Object[]{cassandra_values_v4$put_value$fn__10290.const__0, ((IFn)cassandra_values_v4$put_value$fn__10290.const__1.getRawRoot()).invoke(this.id, n), cassandra_values_v4$put_value$fn__10290.const__2, RT.nth((Object)this.chunks, (int)RT.intCast((Object)((Number)n)))});
        v0 = (IFn)cassandra_values_v4$put_value$fn__10290.const__4.getRawRoot();
        v1 = rets;
        rets = null;
        v2 = val_map;
        val_map = null;
        v3 = n;
        n = null;
        f__10267__auto__10305 = ((IFn)cassandra_values_v4$put_value$fn__10290.const__5.getRawRoot()).invoke(((IFn)cassandra_values_v4$put_value$fn__10290.const__6.getRawRoot()).invoke(cassandra_values_v4$put_value$fn__10290.const__7.getRawRoot()), (Object)new cassandra_values_v4$put_value$fn__10290$fn__10291(this.session, v2, this.id, this.chunks, v3, this.table));
        v4 = f__10267__auto__10305;
        if (Util.classOf((Object)v4) == cassandra_values_v4$put_value$fn__10290.__cached_class__0) ** GOTO lbl15
        if (!(v4 instanceof GetChannel)) {
            v4 = v4;
            cassandra_values_v4$put_value$fn__10290.__cached_class__0 = Util.classOf((Object)v4);
lbl15:
            // 2 sources

            v5 = cassandra_values_v4$put_value$fn__10290.const__8.getRawRoot().invoke(v4);
        } else {
            v5 = ((GetChannel)v4).get_channel();
        }
        v6 = ch__10268__auto__10306 = v5;
        ch__10268__auto__10306 = null;
        ((IFn)cassandra_values_v4$put_value$fn__10290.const__9.getRawRoot()).invoke(v6, (Object)cassandra_values_v4$put_value$fn__10290.const__15, ((IFn)cassandra_values_v4$put_value$fn__10290.const__6.getRawRoot()).invoke(cassandra_values_v4$put_value$fn__10290.const__16.getRawRoot()));
        v7 = f__10267__auto__10305;
        f__10267__auto__10305 = null;
        this = null;
        return v0.invoke(v1, v7);
    }

    static {
        const__0 = RT.keyword(null, (String)"id2");
        const__1 = RT.var((String)"datomic.cassandra-values-v4", (String)"chunk-key");
        const__2 = RT.keyword(null, (String)"val");
        const__4 = RT.var((String)"clojure.core", (String)"conj");
        const__5 = RT.var((String)"datomic.future", (String)"-future-with-channel-impl");
        const__6 = RT.var((String)"clojure.core", (String)"deref");
        const__7 = RT.var((String)"datomic.cassandra-values-v4", (String)"chunk-pool");
        const__8 = RT.var((String)"datomic.future", (String)"get-channel");
        const__9 = RT.var((String)"datomic.future", (String)"add-bounding-warning");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 63, RT.keyword(null, (String)"column"), 34, RT.keyword(null, (String)"file"), "datomic/cassandra_values_v4.clj"});
        const__16 = RT.var((String)"datomic.future", (String)"bounding-warn-seconds");
    }
}

