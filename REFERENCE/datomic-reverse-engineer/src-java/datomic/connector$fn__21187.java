/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.connector.NotificationHandler;

public final class connector$fn__21187
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object msg, Object conn) {
        Object object;
        Object object2 = conn;
        conn = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof NotificationHandler) {
                object = ((NotificationHandler)object3).notify_index();
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        object = const__0.getRawRoot().invoke(object3);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return connector$fn__21187.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.connector", (String)"notify-index");
    }
}

