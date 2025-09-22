/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.iter.MapIter;

public final class iter$fn__11738$__GT_MapIter__11741
extends AFunction {
    public Object invoke(Object f, Object iter2) {
        Object object = f;
        f = null;
        Object object2 = iter2;
        iter2 = null;
        return new MapIter(object, object2);
    }
}

