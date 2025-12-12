/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.btset.IDataSet;

public final class btset$seek
extends AFunction {
    public static Object invokeStatic(Object ds, Object k) {
        Object object;
        Object and__5236__auto__11857;
        Object object2 = and__5236__auto__11857 = ds;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ds;
            ds = null;
            Object object4 = k;
            k = null;
            object = ((IDataSet)object3).seek(object4);
        } else {
            object = and__5236__auto__11857;
            Object var2_2 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return btset$seek.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object ds) {
        Object object;
        Object and__5236__auto__11858;
        Object object2 = and__5236__auto__11858 = ds;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ds;
            ds = null;
            object = ((IDataSet)object3).seek();
        } else {
            object = and__5236__auto__11858;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return btset$seek.invokeStatic(object2);
    }
}

