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
import datomic.treewalk$tree_node_ids$fn__19741;
import datomic.treewalk.TreeWalker;

public final class treewalk$tree_node_ids
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
    public static Object invokeStatic(Object node, Object ids__GT_nodes) {
        v0 = (IFn)treewalk$tree_node_ids.const__0.getRawRoot();
        v1 = (IFn)treewalk$tree_node_ids.const__1.getRawRoot();
        v2 = new treewalk$tree_node_ids$fn__19741(ids__GT_nodes);
        v3 = node;
        if (Util.classOf((Object)v3) == treewalk$tree_node_ids.__cached_class__0) ** GOTO lbl9
        if (!(v3 instanceof TreeWalker)) {
            v3 = v3;
            treewalk$tree_node_ids.__cached_class__0 = Util.classOf((Object)v3);
lbl9:
            // 2 sources

            v4 = ids__GT_nodes;
            ids__GT_nodes = null;
            v5 = treewalk$tree_node_ids.const__2.getRawRoot().invoke(v3, v4);
        } else {
            v6 = ids__GT_nodes;
            ids__GT_nodes = null;
            v5 = ((TreeWalker)v3).subtrees(v6);
        }
        v7 = v1.invoke((Object)v2, v5);
        v8 = node;
        node = null;
        v9 = v8;
        if (Util.classOf((Object)v8) == treewalk$tree_node_ids.__cached_class__1) ** GOTO lbl24
        if (!(v9 instanceof TreeWalker)) {
            v9 = v9;
            treewalk$tree_node_ids.__cached_class__1 = Util.classOf((Object)v9);
lbl24:
            // 2 sources

            v10 = treewalk$tree_node_ids.const__3.getRawRoot().invoke(v9);
        } else {
            v10 = ((TreeWalker)v9).child_node_ids();
        }
        return v0.invoke(v7, v10);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return treewalk$tree_node_ids.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"concat");
        const__1 = RT.var((String)"clojure.core", (String)"mapcat");
        const__2 = RT.var((String)"datomic.treewalk", (String)"subtrees");
        const__3 = RT.var((String)"datomic.treewalk", (String)"child-node-ids");
    }
}

