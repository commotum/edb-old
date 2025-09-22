/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cleanup.Manager;

public final class cleanup$fn__20739$__GT_Manager__20741
extends AFunction {
    public Object invoke(Object phantoms, Object queue2) {
        Object object = phantoms;
        phantoms = null;
        Object object2 = queue2;
        queue2 = null;
        return new Manager(object, object2);
    }
}

