/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.algo;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class lazy$fully_take_while_delivering_tail$fn__19414
extends AFunction {
    Object pred;
    Object p;
    Object coll;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"rest");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__4 = RT.var((String)"datomic.core2.algo.lazy", (String)"fully-take-while-delivering-tail");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"deliver");

    public lazy$fully_take_while_delivering_tail$fn__19414(Object object, Object object2, Object object3) {
        this.pred = object;
        this.p = object2;
        this.coll = object3;
    }

    public Object invoke() {
        Object object;
        Object temp__5802__auto__19416;
        this_.coll = null;
        Object object2 = temp__5802__auto__19416 = ((IFn)const__0.getRawRoot()).invoke(this_.coll);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5802__auto__19416;
            temp__5802__auto__19416 = null;
            Object s = object3;
            Object fst = ((IFn)const__1.getRawRoot()).invoke(s);
            Object rst = ((IFn)const__2.getRawRoot()).invoke(s);
            Object object4 = ((IFn)this_.pred).invoke(fst);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = fst;
                fst = null;
                this_.p = null;
                this_.pred = null;
                Object object6 = rst;
                rst = null;
                lazy$fully_take_while_delivering_tail$fn__19414 this_ = null;
                object = ((IFn)const__3.getRawRoot()).invoke(object5, ((IFn)const__4.getRawRoot()).invoke(this_.p, this_.pred, object6));
            } else {
                this_.p = null;
                Object object7 = s;
                s = null;
                ((IFn)const__5.getRawRoot()).invoke(this_.p, object7);
                object = null;
            }
        } else {
            this_.p = null;
            ((IFn)const__5.getRawRoot()).invoke(this_.p, null);
            object = null;
        }
        return object;
    }
}

