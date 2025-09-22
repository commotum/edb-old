/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.apache.activemq.artemis.api.core.client.ClientSession
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.apache.activemq.artemis.api.core.client.ClientSession;

public final class artemis_client$delete_queue
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"report");

    public static Object invokeStatic(Object session, Object queue2) {
        Object object;
        try {
            Object object2;
            if (((ClientSession)session).isClosed()) {
                object2 = null;
            } else {
                Object object3 = session;
                session = null;
                Object object4 = queue2;
                queue2 = null;
                ((ClientSession)object3).deleteQueue((String)object4);
                object2 = null;
            }
            object = object2;
        }
        catch (Throwable t2) {
            Object t2 = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)t2);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return artemis_client$delete_queue.invokeStatic(object3, object4);
    }
}

