/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.iter.Iterator;

public final class iter$fn__11729$__GT_Iterator__11731
extends AFunction {
    public Object invoke(Object iter2) {
        Object object = iter2;
        iter2 = null;
        return new Iterator(object);
    }
}

