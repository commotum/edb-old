/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db$trim_log$fn__13390;

public final class db$trim_log
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"update-in");
    public static final AFn const__2 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"txes"));

    public static Object invokeStatic(Object memlog2, Object t) {
        Object object = memlog2;
        memlog2 = null;
        Object object2 = t;
        t = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__2, (Object)new db$trim_log$fn__13390(object2));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$trim_log.invokeStatic(object3, object4);
    }
}

