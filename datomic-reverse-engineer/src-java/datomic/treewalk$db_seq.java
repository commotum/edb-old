/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.treewalk.NodeId;

public final class treewalk$db_seq
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object log_root_node, Object index_top_node, Object lookup) {
        v0 = (IFn)treewalk$db_seq.const__0.getRawRoot();
        v1 = (IFn)treewalk$db_seq.const__1.getRawRoot();
        v2 = index_top_node;
        index_top_node = null;
        v3 = v2;
        if (Util.classOf((Object)v2) == treewalk$db_seq.__cached_class__0) ** GOTO lbl10
        if (!(v3 instanceof NodeId)) {
            v3 = v3;
            treewalk$db_seq.__cached_class__0 = Util.classOf((Object)v3);
lbl10:
            // 2 sources

            v4 = treewalk$db_seq.const__2.getRawRoot().invoke(v3);
        } else {
            v4 = ((NodeId)v3).node_id();
        }
        v5 = v1.invoke(v4, lookup);
        v6 = (IFn)treewalk$db_seq.const__3.getRawRoot();
        v7 = log_root_node;
        log_root_node = null;
        v8 = v7;
        if (Util.classOf((Object)v7) == treewalk$db_seq.__cached_class__1) ** GOTO lbl22
        if (!(v8 instanceof NodeId)) {
            v8 = v8;
            treewalk$db_seq.__cached_class__1 = Util.classOf((Object)v8);
lbl22:
            // 2 sources

            v9 = treewalk$db_seq.const__2.getRawRoot().invoke(v8);
        } else {
            v9 = ((NodeId)v8).node_id();
        }
        v10 = lookup;
        lookup = null;
        return v0.invoke(v5, v6.invoke(v9, v10));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return treewalk$db_seq.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"concat");
        const__1 = RT.var((String)"datomic.treewalk", (String)"index-tree-seq");
        const__2 = RT.var((String)"datomic.treewalk", (String)"node-id");
        const__3 = RT.var((String)"datomic.treewalk", (String)"log-tree-seq");
    }
}

