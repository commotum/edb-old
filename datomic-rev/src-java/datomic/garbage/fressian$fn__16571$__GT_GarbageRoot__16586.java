/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.garbage;

import clojure.lang.AFunction;
import datomic.garbage.fressian.GarbageRoot;

public final class fressian$fn__16571$__GT_GarbageRoot__16586
extends AFunction {
    public Object invoke(Object children) {
        Object object = children;
        children = null;
        return new GarbageRoot(object);
    }
}

