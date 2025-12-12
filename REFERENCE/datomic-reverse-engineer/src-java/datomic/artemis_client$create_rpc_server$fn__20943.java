/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.apache.activemq.artemis.api.core.Message
 *  org.apache.activemq.artemis.api.core.client.ClientProducer
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.artemis_client$create_rpc_server$fn__20943$fn__20945;
import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.client.ClientProducer;

public final class artemis_client$create_rpc_server$fn__20943
extends AFunction {
    Object serializer;
    Object producer;
    Object handler;
    Object session;
    Object deserializer;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
    public static final Keyword const__4 = RT.keyword(null, (String)"code");
    public static final Var const__5 = RT.var((String)"datomic.artemis-client", (String)"create-message");

    public artemis_client$create_rpc_server$fn__20943(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.serializer = object;
        this.producer = object2;
        this.handler = object3;
        this.session = object4;
        this.deserializer = object5;
    }

    public Object invoke(Object msg) {
        Object result2;
        Object object;
        Object object2 = msg;
        msg = null;
        Object map__20944 = ((IFn)this.deserializer).invoke(object2);
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__20944);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__20944;
            map__20944 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__20944;
            map__20944 = null;
        }
        Object map__209442 = object;
        Object id = RT.get((Object)map__209442, (Object)const__3);
        Object object5 = map__209442;
        map__209442 = null;
        Object code = RT.get((Object)object5, (Object)const__4);
        Object response = ((IFn)const__5.getRawRoot()).invoke(this.session, (Object)Boolean.FALSE);
        Object object6 = code;
        code = null;
        Object object7 = id;
        id = null;
        Object object8 = result2 = ((IFn)new artemis_client$create_rpc_server$fn__20943$fn__20945(object6, this.handler, object7)).invoke();
        result2 = null;
        ((IFn)this.serializer).invoke(object8, response);
        Object object9 = response;
        response = null;
        ((ClientProducer)this.producer).send((Message)object9);
        return null;
    }
}

