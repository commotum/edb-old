/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.connector.NotificationHandler;

public final class connector$fn__21189
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object msg, Object conn) {
        Object object;
        Object object2;
        block6: {
            block5: {
                Object object3 = conn;
                conn = null;
                object2 = object3;
                if (Util.classOf((Object)object3) == __cached_class__0) break block5;
                if (object2 instanceof NotificationHandler) break block6;
                object2 = object2;
                __cached_class__0 = Util.classOf((Object)object2);
            }
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object4 = msg;
            msg = null;
            Object object5 = iLookupThunk.get(object4);
            if (iLookupThunk == object5) {
                __thunk__0__ = __site__0__.fault(object4);
                object5 = __thunk__0__.get(object4);
            }
            object = const__0.getRawRoot().invoke(object2, object5);
            return object;
        }
        NotificationHandler notificationHandler = (NotificationHandler)object2;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object6 = msg;
        msg = null;
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        object = notificationHandler.notify_sync(object7);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return connector$fn__21189.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.connector", (String)"notify-sync");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
        __thunk__0__ = __site__0__;
    }
}

