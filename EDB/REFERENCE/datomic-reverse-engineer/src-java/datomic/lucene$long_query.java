/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.search.NumericRangeQuery
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.search.NumericRangeQuery;

public final class lucene$long_query
extends AFunction {
    public static Object invokeStatic(Object f, Object l) {
        Object object = f;
        f = null;
        Long l2 = (Long)l;
        Object object2 = l;
        l = null;
        return NumericRangeQuery.newLongRange((String)((String)object), (Long)l2, (Long)((Long)object2), (boolean)Boolean.TRUE, (boolean)Boolean.TRUE);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return lucene$long_query.invokeStatic(object3, object4);
    }
}

