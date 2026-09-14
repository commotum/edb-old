/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 *  org.apache.activemq.artemis.api.core.TransportConfiguration
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.artemis_client$create_transport$fn__20773;
import java.util.Map;
import org.apache.activemq.artemis.api.core.TransportConfiguration;

public final class artemis_client$create_transport
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"partition");
    public static final Object const__3 = 2L;

    public static Object invokeStatic(Object factory, ISeq transport_opts) {
        ISeq iSeq = transport_opts;
        transport_opts = null;
        Object opts_map = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, ((IFn)const__1.getRawRoot()).invoke((Object)new artemis_client$create_transport$fn__20773(), ((IFn)const__2.getRawRoot()).invoke(const__3, (Object)iSeq)));
        Object object = factory;
        factory = null;
        Object object2 = opts_map;
        opts_map = null;
        return new TransportConfiguration((String)object, (Map)object2);
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return artemis_client$create_transport.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

