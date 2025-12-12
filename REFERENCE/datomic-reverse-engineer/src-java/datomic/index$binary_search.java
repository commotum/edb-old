/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class index$binary_search
extends AFunction {
    public static Object invokeStatic(Object coll, Object k, Object cmp) {
        Integer n;
        Object object = k;
        k = null;
        Object object2 = cmp;
        cmp = null;
        int idx = Collections.binarySearch((List)coll, object, (Comparator)object2);
        Integer idx2 = (long)idx < 0L ? (Number)Numbers.num((long)Numbers.unchecked_minus((long)((long)idx + 1L))) : (Number)idx;
        Object object3 = coll;
        coll = null;
        if (Numbers.lt((Object)idx2, (long)RT.count((Object)object3))) {
            n = idx2;
            idx2 = null;
        } else {
            n = null;
        }
        return n;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$binary_search.invokeStatic(object4, object5, object6);
    }
}

