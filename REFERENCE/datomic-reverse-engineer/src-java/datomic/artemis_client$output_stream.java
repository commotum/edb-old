/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.apache.activemq.artemis.api.core.ActiveMQBuffer
 *  org.apache.activemq.artemis.api.core.client.ClientMessage
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.artemis_client$output_stream$fn__20854;
import datomic.artemis_client.proxy$java.io.OutputStream$ff19274a;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;

public final class artemis_client$output_stream
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"init-proxy");

    public static Object invokeStatic(Object msg) {
        Object object = msg;
        msg = null;
        ActiveMQBuffer buf = ((ClientMessage)object).getBodyBuffer();
        OutputStream$ff19274a p__6882__auto__20857 = new OutputStream$ff19274a();
        Object[] objectArray = new Object[2];
        objectArray[0] = "write";
        ActiveMQBuffer activeMQBuffer = buf;
        buf = null;
        objectArray[1] = new artemis_client$output_stream$fn__20854(activeMQBuffer);
        ((IFn)const__0.getRawRoot()).invoke((Object)p__6882__auto__20857, (Object)RT.mapUniqueKeys((Object[])objectArray));
        Object var2_2 = null;
        return p__6882__auto__20857;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return artemis_client$output_stream.invokeStatic(object2);
    }
}

