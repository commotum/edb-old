/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.garbage;

import clojure.lang.AFunction;
import datomic.garbage.fressian.GarbageDir;

public final class fressian$fn__16592$__GT_GarbageDir__16607
extends AFunction {
    public Object invoke(Object children) {
        Object object = children;
        children = null;
        return new GarbageDir(object);
    }
}

