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
import datomic.index$fully_take_while_delivering_tail$fn__15360;

public final class index$fully_take_while_delivering_tail
extends AFunction {
    public static Object invokeStatic(Object p, Object pred2, Object coll) {
        Object object = coll;
        coll = null;
        Object object2 = p;
        p = null;
        Object object3 = pred2;
        pred2 = null;
        return new LazySeq((IFn)new index$fully_take_while_delivering_tail$fn__15360(object, object2, object3));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$fully_take_while_delivering_tail.invokeStatic(object4, object5, object6);
    }
}

