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

public final class treewalk$tree_node_ids$fn__19741
extends AFunction {
    Object ids__GT_nodes;
    public static final Var const__0 = RT.var((String)"datomic.treewalk", (String)"tree-node-ids");

    public treewalk$tree_node_ids$fn__19741(Object object) {
        this.ids__GT_nodes = object;
    }

    public Object invoke(Object p1__19740_SHARP_) {
        Object object = p1__19740_SHARP_;
        p1__19740_SHARP_ = null;
        treewalk$tree_node_ids$fn__19741 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, this_.ids__GT_nodes);
    }
}

