/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.Database;
import datomic.db$unique_violator$fn__13144;

public final class db$unique_violator
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Object const__2 = -1L;
    public static final Keyword const__3 = RT.keyword(null, (String)"avet");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"vector?");

    public static Object invokeStatic(Object db2, Object aid) {
        Object object;
        Object object2 = db2;
        db2 = null;
        Object object3 = aid;
        aid = null;
        Object result2 = ((IFn)const__0.getRawRoot()).invoke((Object)new db$unique_violator$fn__13144(), ((IFn.LLOLO)const__1.getRawRoot()).invokePrim(-1L, -1L, const__2, -1L), ((Database)object2).datoms(const__3, (Object[])((IFn)const__4.getRawRoot()).invoke((Object)Tuple.create((Object)object3))));
        Object object4 = ((IFn)const__5.getRawRoot()).invoke(result2);
        if (object4 != null && object4 != Boolean.FALSE) {
            object = result2;
            result2 = null;
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$unique_violator.invokeStatic(object3, object4);
    }
}

