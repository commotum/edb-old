/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$coerce_tuple$fn__13818;
import java.util.RandomAccess;

public final class db$coerce_tuple
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"coerce-v");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"vector?");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"into");
    public static final Keyword const__7 = RT.keyword(null, (String)"else");

    public static Object invokeStatic(Object tuple2) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), tuple2);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = tuple2;
            tuple2 = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)new db$coerce_tuple$fn__13818(), object3);
        } else {
            Object object4 = ((IFn)const__3.getRawRoot()).invoke(tuple2);
            if (object4 != null && object4 != Boolean.FALSE) {
                object = tuple2;
                tuple2 = null;
            } else if (tuple2 instanceof RandomAccess) {
                Object object5 = tuple2;
                tuple2 = null;
                object = ((IFn)const__6.getRawRoot()).invoke((Object)PersistentVector.EMPTY, object5);
            } else {
                Keyword keyword = const__7;
                if (keyword != null && keyword != Boolean.FALSE) {
                    object = tuple2;
                    tuple2 = null;
                } else {
                    object = null;
                }
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$coerce_tuple.invokeStatic(object2);
    }
}

