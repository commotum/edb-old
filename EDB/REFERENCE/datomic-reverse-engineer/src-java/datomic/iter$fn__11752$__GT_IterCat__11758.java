/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.iter.IterCat;

public final class iter$fn__11752$__GT_IterCat__11758
extends AFunction {
    public Object invoke(Object iter2, Object iters) {
        Object object = iter2;
        iter2 = null;
        Object object2 = iters;
        iters = null;
        return new IterCat(object, object2);
    }
}

