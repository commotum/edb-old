/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.apache.activemq.artemis.api.core.TransportConfiguration
 *  org.apache.activemq.artemis.api.core.client.ActiveMQClient
 *  org.apache.activemq.artemis.api.core.client.ServerLocator
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ServerLocator;

public final class artemis_client$create_server_locator
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"ttl");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__6 = (AFn)Symbol.intern(null, (String)"ttl");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__8 = RT.classForName((String)"org.apache.activemq.artemis.api.core.TransportConfiguration");

    public static Object invokeStatic(Object connector2, Object p__20814) {
        Object object;
        Object ttl;
        Object map__20815;
        Object object2;
        Object object3 = p__20814;
        p__20814 = null;
        Object map__208152 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__208152);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__208152;
            map__208152 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__208152;
            map__208152 = null;
        }
        Object object6 = map__20815 = object2;
        map__20815 = null;
        Object object7 = ttl = RT.get((Object)object6, (Object)const__3);
        if (object7 == null || object7 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__4.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__5.getRawRoot()).invoke((Object)const__6))));
        }
        Object object8 = connector2;
        connector2 = null;
        ServerLocator G__20816 = ActiveMQClient.createServerLocatorWithoutHA((TransportConfiguration[])((TransportConfiguration[])((IFn)const__7.getRawRoot()).invoke(const__8, (Object)Tuple.create((Object)object8))));
        G__20816.setConnectionTTL(RT.longCast((Object)((Number)ttl)));
        G__20816.setProducerWindowSize(RT.intCast((Object)RT.intCast((long)Numbers.multiply((long)256L, (long)1024L))));
        if (Util.equiv((long)-1L, (Object)ttl)) {
            object = ttl;
            ttl = null;
        } else {
            Object object9 = ttl;
            ttl = null;
            object = Numbers.divide((Object)object9, (long)2L);
        }
        G__20816.setClientFailureCheckPeriod(RT.longCast((Object)object));
        ServerLocator serverLocator = G__20816;
        G__20816 = null;
        return serverLocator;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return artemis_client$create_server_locator.invokeStatic(object3, object4);
    }
}

