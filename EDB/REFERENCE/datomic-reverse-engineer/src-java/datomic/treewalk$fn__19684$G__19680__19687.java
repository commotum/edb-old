/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.treewalk.TreeWalker;

public final class treewalk$fn__19684$G__19680__19687
extends AFunction {
    public Object invoke(Object gf_____19685, Object gf__ids__GT_nodes__19686) {
        Object object = gf_____19685;
        gf_____19685 = null;
        Object object2 = gf__ids__GT_nodes__19686;
        gf__ids__GT_nodes__19686 = null;
        return ((TreeWalker)object).subtrees(object2);
    }
}

