/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.treewalk.TreeWalker;

public final class treewalk$log_root_walker$reify__19749$fn$reify__19753
implements TreeWalker,
IObj {
    final IPersistentMap __meta;
    Object v;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__3 = RT.keyword(null, (String)"uuid");

    public treewalk$log_root_walker$reify__19749$fn$reify__19753(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.v = object;
    }

    public treewalk$log_root_walker$reify__19749$fn$reify__19753(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new treewalk$log_root_walker$reify__19749$fn$reify__19753(iPersistentMap, this.v);
    }

    public Object subtrees(Object _) {
        return null;
    }

    public Object child_node_ids() {
        treewalk$log_root_walker$reify__19749$fn$reify__19753 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), (Object)const__3), this_.v);
    }
}

