/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.garbage;

import clojure.lang.AFunction;
import datomic.garbage.fressian.GarbageLeaf;

public final class fressian$fn__16613$__GT_GarbageLeaf__16628
extends AFunction {
    public Object invoke(Object children) {
        Object object = children;
        children = null;
        return new GarbageLeaf(object);
    }
}

