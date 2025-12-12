/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOOL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import datomic.db.Datum;
import datomic.index.IndexedComparator;
import datomic.index.TransposedData;

public final class index$indexed_binary_search
extends AFunction
implements IFn.OOOL {
    public static final Keyword const__9 = RT.keyword(null, (String)"else");

    public static long invokeStatic(Object tdata, Object k, Object cmpi) {
        Number number;
        block3: {
            long low = 0L;
            long high = (long)((TransposedData)tdata).size() - 1L;
            while (low <= high) {
                long mid = (low + high) / 2L;
                long c = ((IndexedComparator)cmpi).compare((Datum)k, (TransposedData)tdata, mid);
                if (c > 0L) {
                    low = mid + 1L;
                    continue;
                }
                if (c < 0L) {
                    high = mid - 1L;
                    continue;
                }
                Keyword keyword = const__9;
                number = keyword != null && keyword != Boolean.FALSE ? Numbers.num((long)mid) : null;
                break block3;
            }
            number = Numbers.num((long)Numbers.unchecked_minus((long)(low + 1L)));
        }
        return number.longValue();
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return new Long(index$indexed_binary_search.invokeStatic(object4, object5, object6));
    }

    public final long invokePrim(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$indexed_binary_search.invokeStatic(object4, object5, object6);
    }
}

