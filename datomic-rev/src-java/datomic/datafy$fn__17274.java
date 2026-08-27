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

public final class datafy$fn__17274
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cast");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"class");

    public static Object invokeStatic(Object obj, Object type) {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(type, obj);
        }
        catch (ClassCastException _) {
            Object object2 = obj;
            Object object3 = obj;
            obj = null;
            Object object4 = type;
            type = null;
            throw (Throwable)new RuntimeException((String)((IFn)const__1.getRawRoot()).invoke((Object)"Could not cast ", object2, (Object)" [", ((IFn)const__2.getRawRoot()).invoke(object3), (Object)"]", (Object)" to ", object4));
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datafy$fn__17274.invokeStatic(object3, object4);
    }
}

