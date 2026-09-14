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
import datomic.index.TransposedData;

public final class index$transposed_data
extends AFunction {
    public static Object invokeStatic(Object es, Object as, Object vs, Object ts, Object ops) {
        long[] eas = Numbers.long_array((Object)Numbers.num((long)Numbers.unchecked_multiply((long)2L, (long)((long[])es).length)));
        long n__5742__auto__15011 = ((long[])es).length;
        for (long i = 0L; i < n__5742__auto__15011; ++i) {
            RT.aset((long[])eas, (int)((int)(2L * i)), (long)((long[])es)[(int)i]);
            RT.aset((long[])eas, (int)((int)(2L * i + 1L)), (long)((int[])as)[(int)i]);
        }
        int n = RT.count((Object)vs);
        long[] lArray = eas;
        eas = null;
        Object object = vs;
        vs = null;
        Object object2 = ts;
        ts = null;
        Object object3 = ops;
        ops = null;
        return new TransposedData(n, lArray, object, object2, object3);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return index$transposed_data.invokeStatic(object6, object7, object8, object9, object10);
    }
}

