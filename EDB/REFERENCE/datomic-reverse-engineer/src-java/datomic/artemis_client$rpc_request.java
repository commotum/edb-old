/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.apache.activemq.artemis.api.core.Message
 *  org.apache.activemq.artemis.api.core.client.ClientProducer
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.artemis_client.RpcClient;
import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.client.ClientProducer;

public final class artemis_client$rpc_request
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"rand-uuid");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"promise");
    public static final Var const__2 = RT.var((String)"datomic.cache", (String)"put");
    public static final Var const__3 = RT.var((String)"datomic.artemis-client", (String)"create-message");
    public static final Keyword const__4 = RT.keyword(null, (String)"id");
    public static final Keyword const__5 = RT.keyword(null, (String)"code");

    public static Object invokeStatic(Object conn, Object request) {
        Object id = ((IFn)const__0.getRawRoot()).invoke();
        Object p = ((IFn)const__1.getRawRoot()).invoke();
        ((IFn)const__2.getRawRoot()).invoke(((RpcClient)conn).response_map, id, p);
        Object msg = ((IFn)const__3.getRawRoot()).invoke(((RpcClient)conn).session, (Object)Boolean.FALSE);
        Object[] objectArray = new Object[4];
        objectArray[0] = const__4;
        Object object = id;
        id = null;
        objectArray[1] = object;
        objectArray[2] = const__5;
        Object object2 = request;
        request = null;
        objectArray[3] = object2;
        ((IFn)((RpcClient)conn).serializer).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), msg);
        Object object3 = conn;
        conn = null;
        Object object4 = msg;
        msg = null;
        ((ClientProducer)((RpcClient)object3).producer).send((Message)object4);
        Object var3_3 = null;
        return p;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return artemis_client$rpc_request.invokeStatic(object3, object4);
    }
}

