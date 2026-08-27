/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.backup.Substorage;

public final class backup$fn__20012$__GT_Substorage__20016
extends AFunction {
    public Object invoke(Object storage, Object prefix) {
        Object object = storage;
        storage = null;
        Object object2 = prefix;
        prefix = null;
        return new Substorage(object, object2);
    }
}

