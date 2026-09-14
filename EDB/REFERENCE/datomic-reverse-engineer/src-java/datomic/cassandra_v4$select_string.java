/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class cassandra_v4$select_string
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"clojure.string", (String)"join");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"name");

    public static Object invokeStatic(Object table, Object p__10118) {
        Object ks;
        Object object = p__10118;
        p__10118 = null;
        Object vec__10119 = object;
        Object id_key = RT.nth((Object)vec__10119, (int)RT.intCast((long)0L), null);
        Object object2 = vec__10119;
        vec__10119 = null;
        Object object3 = ks = object2;
        ks = null;
        Object object4 = table;
        table = null;
        Object object5 = id_key;
        id_key = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)"select ", ((IFn)const__3.getRawRoot()).invoke((Object)", ", ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), object3)), (Object)" from ", object4, (Object)" where ", ((IFn)const__5.getRawRoot()).invoke(object5), (Object)" = ?");
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cassandra_v4$select_string.invokeStatic(object3, object4);
    }
}

