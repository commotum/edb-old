/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.stats$merge_data$fn__17916;

public final class stats$merge_data
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");

    public static Object invokeStatic(Object cmp, Object ds1, Object ds2) {
        Object object;
        Object object2;
        Object and__5236__auto__17919;
        Object object3 = ds1;
        ds1 = null;
        Object ds12 = ((IFn)const__0.getRawRoot()).invoke(object3);
        Object object4 = ds2;
        ds2 = null;
        Object ds22 = ((IFn)const__0.getRawRoot()).invoke(object4);
        Object object5 = and__5236__auto__17919 = ds12;
        if (object5 != null && object5 != Boolean.FALSE) {
            object2 = ds22;
        } else {
            object2 = and__5236__auto__17919;
            and__5236__auto__17919 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            cmp = null;
            ds12 = null;
            ds22 = null;
            object = new LazySeq((IFn)new stats$merge_data$fn__17916(cmp, ds12, ds22));
        } else {
            Object or__5238__auto__17920;
            Object object6 = ds12;
            ds12 = null;
            Object object7 = or__5238__auto__17920 = object6;
            if (object7 != null && object7 != Boolean.FALSE) {
                object = or__5238__auto__17920;
                or__5238__auto__17920 = null;
            } else {
                object = ds22;
                ds22 = null;
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
        return stats$merge_data.invokeStatic(object4, object5, object6);
    }
}

