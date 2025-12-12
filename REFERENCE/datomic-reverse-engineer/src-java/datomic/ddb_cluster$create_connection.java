/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.auth.DefaultAWSCredentialsProviderChain
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

public final class ddb_cluster$create_connection
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"system-root");
    public static final Keyword const__4 = RT.keyword(null, (String)"params");
    public static final Keyword const__5 = RT.keyword(null, (String)"region");
    public static final Keyword const__6 = RT.keyword(null, (String)"override-endpoint");
    public static final Var const__8 = RT.var((String)"datomic.ddb", (String)"client");
    public static final Var const__9 = RT.var((String)"datomic.config", (String)"ddb-client-args");
    public static final Var const__10 = RT.var((String)"datomic.kv-dynamo", (String)"kv-dynamo");
    public static final Var const__11 = RT.var((String)"datomic.kv-cluster", (String)"kv-cluster");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"ddb"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object p__20504) {
        Object kvs;
        Object ddb_client;
        Object creds;
        Object object;
        Object or__5238__auto__20507;
        Object map__20505;
        Object object2;
        Object object3 = p__20504;
        p__20504 = null;
        Object map__205052 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__205052);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__205052;
            map__205052 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__205052;
            map__205052 = null;
        }
        Object cluster_conf = map__20505 = object2;
        Object system_root = RT.get((Object)map__20505, (Object)const__3);
        Object params = RT.get((Object)map__20505, (Object)const__4);
        Object region = RT.get((Object)map__20505, (Object)const__5);
        Object object6 = map__20505;
        map__20505 = null;
        Object override_endpoint = RT.get((Object)object6, (Object)const__6);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = params;
        params = null;
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        Object object9 = or__5238__auto__20507 = object8;
        if (object9 != null && object9 != Boolean.FALSE) {
            object = or__5238__auto__20507;
            or__5238__auto__20507 = null;
        } else {
            object = new DefaultAWSCredentialsProviderChain();
        }
        Object object10 = creds = object;
        creds = null;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__5;
        Object object11 = region;
        region = null;
        objectArray[1] = object11;
        objectArray[2] = const__6;
        Object object12 = override_endpoint;
        override_endpoint = null;
        objectArray[3] = object12;
        Object object13 = ddb_client = ((IFn)const__8.getRawRoot()).invoke(object10, ((IFn)const__9.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
        ddb_client = null;
        Object object14 = system_root;
        system_root = null;
        Object object15 = kvs = ((IFn)const__10.getRawRoot()).invoke(object13, object14);
        kvs = null;
        Object object16 = cluster_conf;
        cluster_conf = null;
        return ((IFn)const__11.getRawRoot()).invoke(object15, object16);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb_cluster$create_connection.invokeStatic(object2);
    }
}

