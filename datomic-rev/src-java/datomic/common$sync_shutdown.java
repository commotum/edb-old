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
import datomic.common.AsyncShutdown;

public final class common$sync_shutdown
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object x) {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = x;
        x = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof AsyncShutdown) {
                object = ((AsyncShutdown)object3).async_shutdown();
                return iFn.invoke(object);
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        object = const__1.getRawRoot().invoke(object3);
        return iFn.invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$sync_shutdown.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.common", (String)"async-shutdown");
    }
}

