/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.query;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class support$incorrect_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__1 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__2 = RT.keyword((String)"cognitect.anomalies", (String)"incorrect");
    public static final Keyword const__3 = RT.keyword((String)"cognitect.anomalies", (String)"message");

    public static Object invokeStatic(Object msg) {
        Object object = msg;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__1;
        objectArray[1] = const__2;
        objectArray[2] = const__3;
        Object object2 = msg;
        msg = null;
        objectArray[3] = object2;
        throw (Throwable)((IFn)const__0.getRawRoot()).invoke(object, (Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return support$incorrect_BANG_.invokeStatic(object2);
    }
}

