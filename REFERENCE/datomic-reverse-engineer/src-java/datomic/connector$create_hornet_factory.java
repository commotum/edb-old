/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class connector$create_hornet_factory
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"host");
    public static final Keyword const__4 = RT.keyword(null, (String)"port");
    public static final Keyword const__5 = RT.keyword(null, (String)"alt-host");
    public static final Keyword const__6 = RT.keyword(null, (String)"encrypt-channel");
    public static final Keyword const__7 = RT.keyword(null, (String)"sslEnabled");
    public static final Keyword const__8 = RT.keyword(null, (String)"keyStorePath");
    public static final Keyword const__9 = RT.keyword(null, (String)"keyStorePassword");
    public static final Keyword const__10 = RT.keyword(null, (String)"ttl");
    public static final Var const__11 = RT.var((String)"datomic.connector", (String)"host-order");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__14 = RT.var((String)"datomic.connector", (String)"try-hornet-connect");
    public static final Var const__15 = RT.var((String)"datomic.artemis-client", (String)"netty-connector-factory");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__19 = RT.var((String)"datomic.connector", (String)"endpoint-error");

    public static Object invokeStatic(Object p__21157, Object ttl) {
        Object result2;
        block3: {
            Object G__21162;
            Object vec__21163;
            Object map__21158;
            Object object;
            Object object2 = p__21157;
            p__21157 = null;
            Object map__211582 = object2;
            Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__211582);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = map__211582;
                map__211582 = null;
                object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
            } else {
                object = map__211582;
                map__211582 = null;
            }
            Object endpoint = map__21158 = object;
            Object host = RT.get((Object)map__21158, (Object)const__3);
            Object port = RT.get((Object)map__21158, (Object)const__4);
            Object alt_host = RT.get((Object)map__21158, (Object)const__5);
            Object object5 = map__21158;
            map__21158 = null;
            Object encrypt_channel = RT.get((Object)object5, (Object)const__6);
            Object[] objectArray = new Object[8];
            objectArray[0] = const__4;
            Object object6 = port;
            port = null;
            objectArray[1] = object6;
            objectArray[2] = const__7;
            Object object7 = encrypt_channel;
            encrypt_channel = null;
            objectArray[3] = object7;
            objectArray[4] = const__8;
            objectArray[5] = "datomic/transactor-key.jks";
            objectArray[6] = const__9;
            objectArray[7] = "transactor";
            IPersistentMap conn_args = RT.mapUniqueKeys((Object[])objectArray);
            Object[] objectArray2 = new Object[2];
            objectArray2[0] = const__10;
            Object object8 = ttl;
            ttl = null;
            objectArray2[1] = object8;
            IPersistentMap session_args = RT.mapUniqueKeys((Object[])objectArray2);
            Object object9 = host;
            host = null;
            Object object10 = alt_host;
            alt_host = null;
            Object object11 = vec__21163 = (G__21162 = ((IFn)const__11.getRawRoot()).invoke(object9, object10));
            vec__21163 = null;
            Object seq__21164 = ((IFn)const__1.getRawRoot()).invoke(object11);
            Object first__21165 = ((IFn)const__12.getRawRoot()).invoke(seq__21164);
            Object object12 = seq__21164;
            seq__21164 = null;
            Object seq__211642 = ((IFn)const__13.getRawRoot()).invoke(object12);
            first__21165 = null;
            seq__211642 = null;
            Object object13 = G__21162;
            G__21162 = null;
            Object G__211622 = object13;
            while (true) {
                Object vec__21166;
                Object object14 = G__211622;
                G__211622 = null;
                Object object15 = vec__21166 = object14;
                vec__21166 = null;
                Object seq__21167 = ((IFn)const__1.getRawRoot()).invoke(object15);
                Object first__21168 = ((IFn)const__12.getRawRoot()).invoke(seq__21167);
                Object object16 = seq__21167;
                seq__21167 = null;
                Object seq__211672 = ((IFn)const__13.getRawRoot()).invoke(object16);
                Object object17 = first__21168;
                first__21168 = null;
                Object host2 = object17;
                Object object18 = seq__211672;
                seq__211672 = null;
                Object more = object18;
                Object object19 = host2;
                host2 = null;
                result2 = ((IFn)const__14.getRawRoot()).invoke(const__15.getRawRoot(), ((IFn)const__16.getRawRoot()).invoke((Object)conn_args, (Object)const__3, object19), (Object)session_args);
                if (!(result2 instanceof Throwable)) break block3;
                Object object20 = more;
                if (object20 == null || object20 == Boolean.FALSE) break;
                Object object21 = more;
                more = null;
                G__211622 = object21;
            }
            Object object22 = result2;
            result2 = null;
            throw (Throwable)((IFn)const__19.getRawRoot()).invoke(endpoint, object22);
        }
        Object object = result2;
        result2 = null;
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return connector$create_hornet_factory.invokeStatic(object3, object4);
    }
}

