/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.concurrent.TimeoutException;

public final class common$bounded_deref
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object ref, Object timeout_ms) {
        Object sentinel = new Object();
        Object object = ref;
        ref = null;
        Object v = ((IFn)const__0.getRawRoot()).invoke(object, timeout_ms, sentinel);
        Object object2 = sentinel;
        sentinel = null;
        if (Util.equiv((Object)v, (Object)object2)) {
            Object object3 = timeout_ms;
            timeout_ms = null;
            throw (Throwable)new TimeoutException((String)((IFn)const__2.getRawRoot()).invoke((Object)"Deref timed out after ", object3, (Object)" msec"));
        }
        Object var3_3 = null;
        return v;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$bounded_deref.invokeStatic(object3, object4);
    }
}

