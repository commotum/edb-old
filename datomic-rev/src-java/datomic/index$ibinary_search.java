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
import datomic.index.IBinarySearch;
import datomic.index.TransposedData;
import java.util.List;

public final class index$ibinary_search
extends AFunction {
    public static Object invokeStatic(Object coll, Object k, Object cmp) {
        Object object = cmp;
        cmp = null;
        Object object2 = k;
        k = null;
        long idx = ((IBinarySearch)object).search((TransposedData)coll, object2);
        long idx2 = idx < 0L ? -(idx + 1L) : idx;
        Object object3 = coll;
        coll = null;
        return idx2 < (long)((List)object3).size() ? (Number)Numbers.num((long)idx2) : (Number)null;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$ibinary_search.invokeStatic(object4, object5, object6);
    }
}

