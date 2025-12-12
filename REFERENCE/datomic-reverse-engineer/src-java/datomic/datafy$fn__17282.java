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

public final class datafy$fn__17282
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"data-to-object");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object _, Object prop, Object obj, Object type) {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(obj, type);
        }
        catch (Exception e2) {
            Object object2 = prop;
            prop = null;
            Object object3 = obj;
            obj = null;
            Object object4 = type;
            type = null;
            Object e2 = null;
            throw (Throwable)new RuntimeException((String)((IFn)const__1.getRawRoot()).invoke((Object)"Could not convert property ", object2, (Object)" value ", object3, (Object)" to type ", object4), e2);
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return datafy$fn__17282.invokeStatic(object5, object6, object7, object8);
    }
}

