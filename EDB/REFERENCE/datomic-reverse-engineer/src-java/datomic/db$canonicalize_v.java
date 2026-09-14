/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.BigInt
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.BigInt;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class db$canonicalize_v
extends AFunction {
    public static final Keyword const__1 = RT.keyword(null, (String)"float");
    public static final Keyword const__5 = RT.keyword(null, (String)"bigint");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"biginteger");
    public static final Keyword const__8 = RT.keyword(null, (String)"default");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"wrong-type-for-attribute");

    public static Object invokeStatic(Object db2, Object attrid, Object v, Object tag) {
        Object object;
        boolean and__5236__auto__13747 = Util.equiv((Object)tag, (Object)const__1);
        if (and__5236__auto__13747 ? v instanceof Double : and__5236__auto__13747) {
            Object object2 = v;
            v = null;
            object = Float.valueOf(RT.uncheckedFloatCast((Object)object2));
        } else {
            boolean and__5236__auto__13748 = Util.equiv((Object)tag, (Object)const__5);
            if (and__5236__auto__13748 ? v instanceof BigInt : and__5236__auto__13748) {
                Object object3 = v;
                v = null;
                object = ((IFn)const__7.getRawRoot()).invoke(object3);
            } else {
                Keyword keyword = const__8;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object object4 = db2;
                    db2 = null;
                    Object object5 = attrid;
                    attrid = null;
                    Object object6 = tag;
                    tag = null;
                    Object object7 = v;
                    v = null;
                    object = ((IFn)const__9.getRawRoot()).invoke(object4, object5, object6, object7);
                } else {
                    object = null;
                }
            }
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
        return db$canonicalize_v.invokeStatic(object5, object6, object7, object8);
    }
}

