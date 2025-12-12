/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOOL
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import datomic.index.IBinarySearch;
import datomic.index.TransposedData;

public final class index$ibtree_search
extends AFunction
implements IFn.OOOL {
    /*
     * WARNING - void declaration
     */
    public static long invokeStatic(Object coll, Object k, Object cmp) {
        void var3_3;
        Object object = cmp;
        cmp = null;
        Object object2 = coll;
        coll = null;
        Object object3 = k;
        k = null;
        long idx = ((IBinarySearch)object).search((TransposedData)object2, object3);
        return idx < 0L ? Numbers.max((long)0L, (long)(-(idx + 1L) - 1L)) : var3_3;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return new Long(index$ibtree_search.invokeStatic(object4, object5, object6));
    }

    public final long invokePrim(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$ibtree_search.invokeStatic(object4, object5, object6);
    }
}

