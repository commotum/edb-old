/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class index$stg_index_size
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"stg-index-segs");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"BYTES_PER_SEG");

    public static Object invokeStatic(Object olookup, Object index2, Object part_size) {
        Object object = olookup;
        olookup = null;
        Object object2 = index2;
        index2 = null;
        Object object3 = part_size;
        part_size = null;
        Object seg_count = ((IFn)const__0.getRawRoot()).invoke(object, object2, object3);
        Float f = Float.valueOf(RT.uncheckedFloatCast((Object)Numbers.unchecked_multiply((Object)seg_count, (Object)const__3.getRawRoot())));
        Object object4 = seg_count;
        seg_count = null;
        return Tuple.create((Object)f, (Object)object4);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$stg_index_size.invokeStatic(object4, object5, object6);
    }
}

