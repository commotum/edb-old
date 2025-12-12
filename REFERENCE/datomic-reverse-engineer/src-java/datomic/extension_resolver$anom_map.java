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

public final class extension_resolver$anom_map
extends AFunction {
    public static final Keyword const__0 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"name");
    public static final Keyword const__3 = RT.keyword((String)"cognitect.anomalies", (String)"message");

    public static Object invokeStatic(Object category, Object msg) {
        Object[] objectArray = new Object[4];
        objectArray[0] = const__0;
        Object object = category;
        category = null;
        objectArray[1] = ((IFn)const__1.getRawRoot()).invoke((Object)"cognitect.anomalies", ((IFn)const__2.getRawRoot()).invoke(object));
        objectArray[2] = const__3;
        Object object2 = msg;
        msg = null;
        objectArray[3] = object2;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return extension_resolver$anom_map.invokeStatic(object3, object4);
    }
}

