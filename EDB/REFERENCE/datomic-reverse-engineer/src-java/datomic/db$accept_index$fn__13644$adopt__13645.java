/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class db$accept_index$fn__13644$adopt__13645
extends AFunction {
    Object nextT;
    public static final Var const__0 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__4 = RT.var((String)"datomic.iter", (String)"inext");

    public db$accept_index$fn__13644$adopt__13645(Object object) {
        this.nextT = object;
    }

    public Object invoke(Object nidx, Object idx) {
        Object object = nidx;
        nidx = null;
        Object nidx2 = object;
        Object object2 = idx;
        idx = null;
        Object iter2 = ((IFn)const__0.getRawRoot()).invoke(object2);
        while (true) {
            Object object3;
            Object temp__5455__auto__13647;
            Object object4 = temp__5455__auto__13647 = ((IFn)const__1.getRawRoot()).invoke(iter2);
            if (object4 == null || object4 == Boolean.FALSE) break;
            Object object5 = temp__5455__auto__13647;
            temp__5455__auto__13647 = null;
            Object d = object5;
            if (Numbers.lt((long)((IDatum)d).getT(), (Object)this.nextT)) {
                object3 = nidx2;
                nidx2 = null;
            } else {
                Object object6 = nidx2;
                nidx2 = null;
                Object object7 = d;
                d = null;
                object3 = ((IFn)const__3.getRawRoot()).invoke(object6, object7);
            }
            Object object8 = iter2;
            iter2 = null;
            iter2 = ((IFn)const__4.getRawRoot()).invoke(object8);
            nidx2 = object3;
        }
        Object var3_3 = null;
        return nidx2;
    }
}

