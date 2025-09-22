/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.artemis_client$fressian_producer$reify__20867;

public final class artemis_client$fressian_producer
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 249, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object hornet_session, Object hornet_producer, Object write_handlers2) {
        Object object = hornet_session;
        hornet_session = null;
        Object object2 = hornet_producer;
        hornet_producer = null;
        Object object3 = write_handlers2;
        write_handlers2 = null;
        return ((IObj)new artemis_client$fressian_producer$reify__20867(null, object, object2, object3)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return artemis_client$fressian_producer.invokeStatic(object4, object5, object6);
    }
}

