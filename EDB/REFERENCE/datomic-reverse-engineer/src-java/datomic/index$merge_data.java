/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import datomic.index$merge_data$fn__15378;

public final class index$merge_data
extends AFunction {
    public static Object invokeStatic(Object cmp, Object ds1, Object ds2) {
        Object object;
        Object object2;
        Object and__5236__auto__15381;
        Object object3 = and__5236__auto__15381 = ds1;
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = ds2;
        } else {
            object2 = and__5236__auto__15381;
            and__5236__auto__15381 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            ds2 = null;
            ds1 = null;
            cmp = null;
            object = new LazySeq((IFn)new index$merge_data$fn__15378(ds2, ds1, cmp));
        } else {
            Object or__5238__auto__15382;
            Object object4 = ds1;
            ds1 = null;
            Object object5 = or__5238__auto__15382 = object4;
            if (object5 != null && object5 != Boolean.FALSE) {
                object = or__5238__auto__15382;
                or__5238__auto__15382 = null;
            } else {
                object = ds2;
                Object var2_2 = null;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$merge_data.invokeStatic(object4, object5, object6);
    }
}

