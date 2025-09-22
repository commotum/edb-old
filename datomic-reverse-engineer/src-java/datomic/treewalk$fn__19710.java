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
import datomic.treewalk.TreeWalker;

public final class treewalk$fn__19710
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object root, Object ids__GT_nodes) {
        Object object;
        Object object2 = ids__GT_nodes;
        ids__GT_nodes = null;
        IFn iFn = (IFn)object2;
        Object object3 = root;
        root = null;
        Object object4 = object3;
        if (Util.classOf((Object)object3) != __cached_class__0) {
            if (object4 instanceof TreeWalker) {
                object = ((TreeWalker)object4).child_node_ids();
                return iFn.invoke(object);
            }
            object4 = object4;
            __cached_class__0 = Util.classOf((Object)object4);
        }
        object = const__0.getRawRoot().invoke(object4);
        return iFn.invoke(object);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return treewalk$fn__19710.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.treewalk", (String)"child-node-ids");
    }
}

