/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.iter.MapIter;

public final class iter$map
extends AFunction {
    public static Object invokeStatic(Object f, Object iter2) {
        MapIter mapIter;
        Object object = iter2;
        if (object != null && object != Boolean.FALSE) {
            f = null;
            iter2 = null;
            mapIter = new MapIter(f, iter2);
        } else {
            mapIter = null;
        }
        return mapIter;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return iter$map.invokeStatic(object3, object4);
    }
}

