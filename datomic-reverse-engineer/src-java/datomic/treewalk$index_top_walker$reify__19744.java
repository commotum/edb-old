/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.treewalk.TreeWalker;

public final class treewalk$index_top_walker$reify__19744
implements TreeWalker,
IObj {
    final IPersistentMap __meta;
    Object top;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;

    public treewalk$index_top_walker$reify__19744(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.top = object;
    }

    public treewalk$index_top_walker$reify__19744(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new treewalk$index_top_walker$reify__19744(iPersistentMap, this.top);
    }

    /*
     * Unable to fully structure code
     */
    public Object subtrees(Object ids__GT_nodes) {
        v0 = ids__GT_nodes;
        ids__GT_nodes = null;
        v1 = (IFn)v0;
        v2 = this;
        if (Util.classOf((Object)v2) == treewalk$index_top_walker$reify__19744.__cached_class__0) ** GOTO lbl9
        if (!(v2 instanceof TreeWalker)) {
            v2 = v2;
            treewalk$index_top_walker$reify__19744.__cached_class__0 = Util.classOf((Object)v2);
lbl9:
            // 2 sources

            v3 = treewalk$index_top_walker$reify__19744.const__5.getRawRoot().invoke((Object)v2);
        } else {
            v3 = ((TreeWalker)v2).child_node_ids();
        }
        this = null;
        return v1.invoke(v3);
    }

    public Object child_node_ids() {
        treewalk$index_top_walker$reify__19744 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(this_.top, const__4.getRawRoot())));
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"map");
        const__1 = RT.var((String)"clojure.core", (String)"str");
        const__2 = RT.var((String)"clojure.core", (String)"remove");
        const__3 = RT.var((String)"clojure.core", (String)"nil?");
        const__4 = RT.var((String)"datomic.treewalk", (String)"index-root-keys");
        const__5 = RT.var((String)"datomic.treewalk", (String)"child-node-ids");
    }
}

