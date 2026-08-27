/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;

public final class query$xf_tuple
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"conj!");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"persistent!");

    public static Object invokeStatic(Object fv, Object tuple2) {
        Object result2;
        Object G__19483;
        Object vec__19487;
        Object G__19482;
        Object vec__19484;
        Object result3 = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentVector.EMPTY);
        Object object = tuple2;
        tuple2 = null;
        Object object2 = vec__19484 = (G__19482 = object);
        vec__19484 = null;
        Object seq__19485 = ((IFn)const__1.getRawRoot()).invoke(object2);
        Object first__19486 = ((IFn)const__2.getRawRoot()).invoke(seq__19485);
        Object object3 = seq__19485;
        seq__19485 = null;
        Object seq__194852 = ((IFn)const__3.getRawRoot()).invoke(object3);
        first__19486 = null;
        seq__194852 = null;
        Object object4 = fv;
        fv = null;
        Object object5 = vec__19487 = (G__19483 = object4);
        vec__19487 = null;
        Object seq__19488 = ((IFn)const__1.getRawRoot()).invoke(object5);
        Object first__19489 = ((IFn)const__2.getRawRoot()).invoke(seq__19488);
        Object object6 = seq__19488;
        seq__19488 = null;
        Object seq__194882 = ((IFn)const__3.getRawRoot()).invoke(object6);
        first__19489 = null;
        seq__194882 = null;
        Object object7 = result3;
        result3 = null;
        Object result4 = object7;
        Object object8 = G__19482;
        G__19482 = null;
        Object G__194822 = object8;
        Object object9 = G__19483;
        G__19483 = null;
        Object G__194832 = object9;
        while (true) {
            Object vec__19493;
            Object vec__19490;
            Object object10 = result4;
            result4 = null;
            result2 = object10;
            Object object11 = G__194822;
            G__194822 = null;
            Object object12 = vec__19490 = object11;
            vec__19490 = null;
            Object seq__19491 = ((IFn)const__1.getRawRoot()).invoke(object12);
            Object first__19492 = ((IFn)const__2.getRawRoot()).invoke(seq__19491);
            Object object13 = seq__19491;
            seq__19491 = null;
            Object seq__194912 = ((IFn)const__3.getRawRoot()).invoke(object13);
            Object object14 = first__19492;
            first__19492 = null;
            Object col = object14;
            Object object15 = seq__194912;
            seq__194912 = null;
            Object tuple3 = object15;
            Object object16 = G__194832;
            G__194832 = null;
            Object object17 = vec__19493 = object16;
            vec__19493 = null;
            Object seq__19494 = ((IFn)const__1.getRawRoot()).invoke(object17);
            Object first__19495 = ((IFn)const__2.getRawRoot()).invoke(seq__19494);
            Object object18 = seq__19494;
            seq__19494 = null;
            Object seq__194942 = ((IFn)const__3.getRawRoot()).invoke(object18);
            Object object19 = first__19495;
            first__19495 = null;
            Object f = object19;
            Object object20 = seq__194942;
            seq__194942 = null;
            Object fv2 = object20;
            Object object21 = f;
            if (object21 == null || object21 == Boolean.FALSE) break;
            Object object22 = result2;
            result2 = null;
            Object object23 = f;
            f = null;
            Object object24 = col;
            col = null;
            Object object25 = tuple3;
            tuple3 = null;
            Object object26 = fv2;
            fv2 = null;
            G__194832 = object26;
            G__194822 = object25;
            result4 = ((IFn)const__4.getRawRoot()).invoke(object22, ((IFn)object23).invoke(object24));
        }
        Object object27 = result2;
        result2 = null;
        return ((IFn)const__5.getRawRoot()).invoke(object27);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$xf_tuple.invokeStatic(object3, object4);
    }
}

