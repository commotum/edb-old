/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Datom;

public final class db$updates_v_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"compare");

    public static Object invokeStatic(Object d1, Object d2) {
        Object object;
        boolean and__5236__auto__13404 = ((Datom)d1).added();
        if (and__5236__auto__13404) {
            boolean and__5236__auto__13403 = Util.equiv((Object)((Datom)d1).e(), (Object)((Datom)d2).e());
            if (and__5236__auto__13403) {
                boolean and__5236__auto__13402 = Util.equiv((Object)((Datom)d1).a(), (Object)((Datom)d2).a());
                if (and__5236__auto__13402) {
                    Object and__5236__auto__13401;
                    Object object2 = and__5236__auto__13401 = ((IFn)const__1.getRawRoot()).invoke((Object)(Numbers.isZero((long)((IFn.OOL)const__3.getRawRoot()).invokePrim(((Datom)d1).v(), ((Datom)d2).v())) ? Boolean.TRUE : Boolean.FALSE));
                    if (object2 != null && object2 != Boolean.FALSE) {
                        boolean and__5236__auto__13400 = ((Datom)d2).added();
                        if (and__5236__auto__13400) {
                            Object object3 = d2;
                            d2 = null;
                            Object object4 = d1;
                            d1 = null;
                            object = Numbers.lt((Object)((Datom)object3).tx(), (Object)((Datom)object4).tx()) ? Boolean.TRUE : Boolean.FALSE;
                        } else {
                            object = and__5236__auto__13400 ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        object = and__5236__auto__13401;
                        and__5236__auto__13401 = null;
                    }
                } else {
                    object = and__5236__auto__13402 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object = and__5236__auto__13403 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object = and__5236__auto__13404 ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$updates_v_QMARK_.invokeStatic(object3, object4);
    }
}

