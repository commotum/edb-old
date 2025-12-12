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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class error$fn__656
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__1 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__2 = RT.keyword((String)"cognitect.anomalies", (String)"incorrect");
    public static final Keyword const__3 = RT.keyword((String)"cognitect.anomalies", (String)"message");

    public static Object invokeStatic(Object m, Object cls, Object msg) {
        Object[] objectArray = new Object[4];
        objectArray[0] = const__1;
        objectArray[1] = const__2;
        objectArray[2] = const__3;
        Object object = msg;
        msg = null;
        objectArray[3] = object;
        Object object2 = m;
        m = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), object2);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return error$fn__656.invokeStatic(object4, object5, object6);
    }
}

