/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class datalog$fn__18233$fn__18253$fn__18254
extends AFunction {
    Object bound;
    Object whilev;
    Object d;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"not");

    public datalog$fn__18233$fn__18253$fn__18254(Object object, Object object2, Object object3) {
        this.bound = object;
        this.whilev = object2;
        this.d = object3;
    }

    public Object invoke(Object p1__18218_SHARP_) {
        Object object;
        boolean and__5236__auto__18259 = Util.equiv((long)((IDatum)this_.d).getE(), (long)((IDatum)p1__18218_SHARP_).getE());
        if (and__5236__auto__18259) {
            boolean and__5236__auto__18258 = Util.equiv((long)((IDatum)this_.d).getA(), (long)((IDatum)p1__18218_SHARP_).getA());
            if (and__5236__auto__18258) {
                Object or__5238__auto__18257;
                Object object2;
                Object and__5236__auto__18256;
                Object object3 = and__5236__auto__18256 = ((IFn)const__1.getRawRoot()).invoke(RT.aget((Object[])((Object[])this_.bound), (int)((int)2L)));
                if (object3 != null && object3 != Boolean.FALSE) {
                    object2 = Util.identical((Object)this_.whilev, null) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    object2 = and__5236__auto__18256;
                    and__5236__auto__18256 = null;
                }
                Object object4 = or__5238__auto__18257 = object2;
                if (object4 != null && object4 != Boolean.FALSE) {
                    object = or__5238__auto__18257;
                    or__5238__auto__18257 = null;
                } else {
                    datalog$fn__18233$fn__18253$fn__18254 this_;
                    Object object5 = RT.aget((Object[])((Object[])this_.bound), (int)((int)2L));
                    if (object5 != null && object5 != Boolean.FALSE) {
                        Object object6 = p1__18218_SHARP_;
                        p1__18218_SHARP_ = null;
                        this_ = null;
                        object = Util.equiv((Object)((IDatum)this_.d).getV(), (Object)((IDatum)object6).getV()) ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        Object object7 = p1__18218_SHARP_;
                        p1__18218_SHARP_ = null;
                        this_ = null;
                        object = ((IFn)this_.whilev).invoke(((IDatum)object7).getV());
                    }
                }
            } else {
                object = and__5236__auto__18258 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object = and__5236__auto__18259 ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }
}

