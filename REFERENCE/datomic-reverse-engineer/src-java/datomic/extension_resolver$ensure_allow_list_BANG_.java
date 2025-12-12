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

public final class extension_resolver$ensure_allow_list_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"coll?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__2 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__3 = RT.keyword((String)"cognitect.anomalies", (String)"incorrect");
    public static final Keyword const__4 = RT.keyword(null, (String)"value");

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static Object invokeStatic(Object allow_list) {
        Object object = allow_list;
        if (object == null) return null;
        if (object == Boolean.FALSE) return null;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(allow_list);
        if (object2 != null && object2 != Boolean.FALSE) {
            return null;
        }
        Object[] objectArray = new Object[4];
        objectArray[0] = const__2;
        objectArray[1] = const__3;
        objectArray[2] = const__4;
        Object object3 = allow_list;
        allow_list = null;
        objectArray[3] = object3;
        throw (Throwable)((IFn)const__1.getRawRoot()).invoke((Object)"xforms expects a vector", (Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return extension_resolver$ensure_allow_list_BANG_.invokeStatic(object2);
    }
}

