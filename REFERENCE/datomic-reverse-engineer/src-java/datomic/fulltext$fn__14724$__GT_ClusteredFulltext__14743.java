/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.fulltext.ClusteredFulltext;

public final class fulltext$fn__14724$__GT_ClusteredFulltext__14743
extends AFunction {
    public Object invoke(Object olookup, Object root) {
        Object object = olookup;
        olookup = null;
        Object object2 = root;
        root = null;
        return new ClusteredFulltext(object, object2);
    }
}

