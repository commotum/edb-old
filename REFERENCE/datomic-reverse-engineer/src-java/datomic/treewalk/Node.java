/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.treewalk;

import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.treewalk.NodeId;
import datomic.treewalk.TreeWalker;

public final class Node
implements TreeWalker,
NodeId,
IType {
    public final Object id;
    public final Object walker;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;

    public Node(Object object, Object object2) {
        this.id = object;
        this.walker = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"walker"));
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object subtrees(Object ids__GT_nodes) {
        Object object;
        Object object2 = this_.walker;
        if (Util.classOf((Object)object2) != __cached_class__1) {
            if (object2 instanceof TreeWalker) {
                Object object3 = ids__GT_nodes;
                ids__GT_nodes = null;
                object = ((TreeWalker)object2).subtrees(object3);
                return object;
            }
            object2 = object2;
            __cached_class__1 = Util.classOf((Object)object2);
        }
        Object object4 = ids__GT_nodes;
        ids__GT_nodes = null;
        Node this_ = null;
        object = const__1.getRawRoot().invoke(object2, object4);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object child_node_ids() {
        Object object;
        Object object2 = this_.walker;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof TreeWalker) {
                object = ((TreeWalker)object2).child_node_ids();
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Node this_ = null;
        object = const__0.getRawRoot().invoke(object2);
        return object;
    }

    public Object node_id() {
        return this.id;
    }

    static {
        const__0 = RT.var((String)"datomic.treewalk", (String)"child-node-ids");
        const__1 = RT.var((String)"datomic.treewalk", (String)"subtrees");
    }
}

