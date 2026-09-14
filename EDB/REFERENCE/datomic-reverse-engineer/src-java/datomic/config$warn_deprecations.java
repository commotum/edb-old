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

public final class config$warn_deprecations
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"*out*");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"*err*");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public static Object invokeStatic() {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)System.getProperty("datomic.objectCacheBytes"));
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3;
            ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3, const__4.get()));
            try {
                object3 = ((IFn)const__5.getRawRoot()).invoke((Object)"Warning: datomic.objectCacheBytes has been renamed to datomic.objectCacheMax, please change your configuration!");
            }
            finally {
                ((IFn)const__6.getRawRoot()).invoke();
            }
            object = object3;
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke() {
        return config$warn_deprecations.invokeStatic();
    }
}

