/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.iter.Iter;

public final class iter$iget
extends AFunction {
    public static Object invokeStatic(Object iter2) {
        Object object;
        Object and__5236__auto__11795;
        Object object2 = and__5236__auto__11795 = iter2;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = iter2;
            iter2 = null;
            object = ((Iter)object3).get();
        } else {
            object = and__5236__auto__11795;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return iter$iget.invokeStatic(object2);
    }
}

