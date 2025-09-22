/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class anomalies$fault
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__1 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__2 = RT.keyword((String)"cognitect.anomalies", (String)"fault");
    public static final Keyword const__3 = RT.keyword((String)"datomic.core2.anomalies", (String)"exception");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"Throwable->map");
    public static final Keyword const__5 = RT.keyword((String)"cognitect.anomalies", (String)"message");

    public static Object invokeStatic(Object t) {
        IPersistentMap iPersistentMap;
        String temp__5804__auto__19456;
        IFn iFn = (IFn)const__0.getRawRoot();
        IPersistentMap iPersistentMap2 = RT.mapUniqueKeys((Object[])new Object[]{const__1, const__2, const__3, ((IFn)const__4.getRawRoot()).invoke(t)});
        Object object = t;
        t = null;
        String string = temp__5804__auto__19456 = ((Throwable)object).getMessage();
        if (string != null && string != Boolean.FALSE) {
            String string2 = temp__5804__auto__19456;
            temp__5804__auto__19456 = null;
            String msg = string2;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__5;
            String string3 = msg;
            msg = null;
            objectArray[1] = string3;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        return iFn.invoke((Object)iPersistentMap2, iPersistentMap);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return anomalies$fault.invokeStatic(object2);
    }
}

