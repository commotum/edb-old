/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class extension_resolver$anomaly_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__1 = RT.var((String)"datomic.extension-resolver", (String)"anom-map");

    public static Object invokeStatic(Object name, Object msg, Object cause) {
        Object object = msg;
        Object object2 = name;
        name = null;
        Object object3 = msg;
        msg = null;
        Object object4 = cause;
        cause = null;
        throw (Throwable)((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke(object2, object3), object4);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return extension_resolver$anomaly_BANG_.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object name, Object msg) {
        Object object = msg;
        Object object2 = name;
        name = null;
        Object object3 = msg;
        msg = null;
        throw (Throwable)((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke(object2, object3));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return extension_resolver$anomaly_BANG_.invokeStatic(object3, object4);
    }
}

