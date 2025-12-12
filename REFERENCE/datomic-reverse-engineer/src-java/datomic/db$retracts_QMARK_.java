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

public final class db$retracts_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"compare");

    public static Object invokeStatic(Object d1, Object d2) {
        Object object;
        Object and__5236__auto__13398;
        Object object2 = and__5236__auto__13398 = ((IFn)const__0.getRawRoot()).invoke((Object)(((Datom)d1).added() ? Boolean.TRUE : Boolean.FALSE));
        if (object2 != null && object2 != Boolean.FALSE) {
            boolean and__5236__auto__13397 = Util.equiv((Object)((Datom)d1).e(), (Object)((Datom)d2).e());
            if (and__5236__auto__13397) {
                boolean and__5236__auto__13396 = Util.equiv((Object)((Datom)d1).a(), (Object)((Datom)d2).a());
                if (and__5236__auto__13396) {
                    boolean and__5236__auto__13395 = Numbers.isZero((long)((IFn.OOL)const__3.getRawRoot()).invokePrim(((Datom)d1).v(), ((Datom)d2).v()));
                    if (and__5236__auto__13395) {
                        boolean and__5236__auto__13394 = ((Datom)d2).added();
                        if (and__5236__auto__13394) {
                            Object object3 = d2;
                            d2 = null;
                            Object object4 = d1;
                            d1 = null;
                            object = Numbers.lt((Object)((Datom)object3).tx(), (Object)((Datom)object4).tx()) ? Boolean.TRUE : Boolean.FALSE;
                        } else {
                            object = and__5236__auto__13394 ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        object = and__5236__auto__13395 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    object = and__5236__auto__13396 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object = and__5236__auto__13397 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object = and__5236__auto__13398;
            Object var2_2 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$retracts_QMARK_.invokeStatic(object3, object4);
    }
}

