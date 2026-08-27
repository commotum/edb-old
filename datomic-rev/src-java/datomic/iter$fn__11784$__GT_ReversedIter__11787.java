/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.iter.ReversedIter;

public final class iter$fn__11784$__GT_ReversedIter__11787
extends AFunction {
    public Object invoke(Object iter2) {
        Object object = iter2;
        iter2 = null;
        return new ReversedIter(object);
    }
}

