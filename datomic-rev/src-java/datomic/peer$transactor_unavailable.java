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

public final class peer$transactor_unavailable
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__1 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__2 = RT.keyword((String)"cognitect.anomalies", (String)"unavailable");
    public static final Keyword const__3 = RT.keyword((String)"cognitect.anomalies", (String)"message");

    public static Object invokeStatic() {
        String msg;
        String string = msg = "Transactor not available";
        Object[] objectArray = new Object[4];
        objectArray[0] = const__1;
        objectArray[1] = const__2;
        objectArray[2] = const__3;
        String string2 = msg;
        msg = null;
        objectArray[3] = string2;
        return ((IFn)const__0.getRawRoot()).invoke((Object)string, (Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke() {
        return peer$transactor_unavailable.invokeStatic();
    }
}

