/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.artemis_client.HornetImpl;

public final class artemis_client$start_session
extends RestFn {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object factory, Object creds, ISeq args) {
        Object object;
        Object object2 = factory;
        factory = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof HornetImpl) {
                Object object4 = creds;
                creds = null;
                ISeq iSeq = args;
                args = null;
                object = ((HornetImpl)object3).start_session_STAR_(object4, iSeq);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object5 = creds;
        creds = null;
        ISeq iSeq = args;
        args = null;
        object = const__0.getRawRoot().invoke(object3, object5, (Object)iSeq);
        return object;
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return artemis_client$start_session.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }

    static {
        const__0 = RT.var((String)"datomic.artemis-client", (String)"start-session*");
    }
}

