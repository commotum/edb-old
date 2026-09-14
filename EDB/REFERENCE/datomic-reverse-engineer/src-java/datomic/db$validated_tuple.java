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

public final class db$validated_tuple
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"coerce-tuple");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"require-tuple-ids");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"tuple-elem?");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"valid-tuple-assert?");

    public static Object invokeStatic(Object db2, Object op, Object attr, Object v) {
        Object object;
        Object object2 = db2;
        db2 = null;
        Object object3 = v;
        v = null;
        Object tup = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object2, attr, object3));
        Object object4 = op;
        op = null;
        if (Util.equiv((long)1L, (Object)object4)) {
            Object and__5236__auto__13844;
            Object object5 = and__5236__auto__13844 = tup;
            if (object5 != null && object5 != Boolean.FALSE) {
                Object and__5236__auto__13843;
                Object object6 = and__5236__auto__13843 = ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), tup);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object and__5236__auto__13842;
                    Object object7 = attr;
                    attr = null;
                    Object object8 = and__5236__auto__13842 = ((IFn)const__6.getRawRoot()).invoke(object7, tup);
                    if (object8 != null && object8 != Boolean.FALSE) {
                        object = tup;
                        tup = null;
                    } else {
                        object = and__5236__auto__13842;
                        and__5236__auto__13842 = null;
                    }
                } else {
                    object = and__5236__auto__13843;
                    and__5236__auto__13843 = null;
                }
            } else {
                object = and__5236__auto__13844;
                and__5236__auto__13844 = null;
            }
        } else {
            object = tup;
            tup = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$validated_tuple.invokeStatic(object5, object6, object7, object8);
    }
}

