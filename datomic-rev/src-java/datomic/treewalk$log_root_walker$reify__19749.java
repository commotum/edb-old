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
import datomic.treewalk$log_root_walker$reify__19749$fn__19751;
import datomic.treewalk.TreeWalker;

public final class treewalk$log_root_walker$reify__19749
implements TreeWalker,
IObj {
    final IPersistentMap __meta;
    Object lookup;
    Object allow_missing_QMARK_;
    Object root;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__3 = RT.keyword(null, (String)"uuid");

    public treewalk$log_root_walker$reify__19749(IPersistentMap iPersistentMap, Object object, Object object2, Object object3) {
        this.__meta = iPersistentMap;
        this.lookup = object;
        this.allow_missing_QMARK_ = object2;
        this.root = object3;
    }

    public treewalk$log_root_walker$reify__19749(Object object, Object object2, Object object3) {
        this(null, object, object2, object3);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new treewalk$log_root_walker$reify__19749(iPersistentMap, this.lookup, this.allow_missing_QMARK_, this.root);
    }

    public Object subtrees(Object ids__GT_nodes) {
        treewalk$log_root_walker$reify__19749 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new treewalk$log_root_walker$reify__19749$fn__19751(this_.lookup, this_.allow_missing_QMARK_), this_.root);
    }

    public Object child_node_ids() {
        treewalk$log_root_walker$reify__19749 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), (Object)const__3), this_.root);
    }
}

