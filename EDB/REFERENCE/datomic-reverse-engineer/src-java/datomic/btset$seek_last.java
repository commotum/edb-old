/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.btset.IDataSet;

public final class btset$seek_last
extends AFunction {
    public static Object invokeStatic(Object ds) {
        Object object;
        Object and__5236__auto__11860;
        Object object2 = and__5236__auto__11860 = ds;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ds;
            ds = null;
            object = ((IDataSet)object3).seekLast();
        } else {
            object = and__5236__auto__11860;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return btset$seek_last.invokeStatic(object2);
    }
}

