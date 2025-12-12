/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class index$btree_search
extends AFunction {
    public static Object invokeStatic(Object coll, Object k, Object cmp) {
        Object object = coll;
        coll = null;
        Object object2 = k;
        k = null;
        Object object3 = cmp;
        cmp = null;
        int idx = Collections.binarySearch((List)object, object2, (Comparator)object3);
        return (long)idx < 0L ? (Number)Numbers.num((long)Numbers.max((long)0L, (long)(-((long)idx + 1L) - 1L))) : (Number)idx;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$btree_search.invokeStatic(object4, object5, object6);
    }
}

