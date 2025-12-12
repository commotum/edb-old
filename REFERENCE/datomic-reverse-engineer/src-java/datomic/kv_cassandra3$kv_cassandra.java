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
import datomic.kv_cassandra3$kv_cassandra$fn__23658;
import datomic.kv_cassandra3.KVCassandra3;

public final class kv_cassandra3$kv_cassandra
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"host");
    public static final Keyword const__4 = RT.keyword(null, (String)"table");
    public static final Keyword const__5 = RT.keyword(null, (String)"session");
    public static final Object const__7 = 9042L;
    public static final Var const__12 = RT.var((String)"datomic.kv-cassandra3", (String)"sessions");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"port"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"user"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"password"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"local-datacenter"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"ssl"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public static Object invokeStatic(Object endpoint) {
        Object session;
        Object map__23657;
        Object object;
        Object object2;
        Object or__5238__auto__23668;
        Object object3;
        Object or__5238__auto__23667;
        Object object4;
        Object or__5238__auto__23666;
        Object object5;
        Object map__23656 = endpoint;
        Object object6 = ((IFn)const__0.getRawRoot()).invoke(map__23656);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = map__23656;
            map__23656 = null;
            object5 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object7)));
        } else {
            object5 = map__23656;
            map__23656 = null;
        }
        Object map__236562 = object5;
        Object host = RT.get((Object)map__236562, (Object)const__3);
        Object table = RT.get((Object)map__236562, (Object)const__4);
        Object object8 = map__236562;
        map__236562 = null;
        Object provided_session = RT.get((Object)object8, (Object)const__5);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object9 = endpoint;
        Object object10 = iLookupThunk.get(object9);
        if (iLookupThunk == object10) {
            __thunk__0__ = __site__0__.fault(object9);
            object10 = __thunk__0__.get(object9);
        }
        Object object11 = or__5238__auto__23666 = object10;
        if (object11 != null && object11 != Boolean.FALSE) {
            object4 = or__5238__auto__23666;
            or__5238__auto__23666 = null;
        } else {
            object4 = const__7;
        }
        Object port = object4;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object12 = endpoint;
        Object object13 = iLookupThunk2.get(object12);
        if (iLookupThunk2 == object13) {
            __thunk__1__ = __site__1__.fault(object12);
            object13 = __thunk__1__.get(object12);
        }
        Object user = object13;
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object14 = endpoint;
        Object object15 = iLookupThunk3.get(object14);
        if (iLookupThunk3 == object15) {
            __thunk__2__ = __site__2__.fault(object14);
            object15 = __thunk__2__.get(object14);
        }
        Object password = object15;
        ILookupThunk iLookupThunk4 = __thunk__3__;
        Object object16 = endpoint;
        Object object17 = iLookupThunk4.get(object16);
        if (iLookupThunk4 == object17) {
            __thunk__3__ = __site__3__.fault(object16);
            object17 = __thunk__3__.get(object16);
        }
        Object object18 = or__5238__auto__23667 = object17;
        if (object18 != null && object18 != Boolean.FALSE) {
            object3 = or__5238__auto__23667;
            or__5238__auto__23667 = null;
        } else {
            object3 = "datacenter1";
        }
        Object local_datacenter = object3;
        ILookupThunk iLookupThunk5 = __thunk__4__;
        Object object19 = endpoint;
        Object object20 = iLookupThunk5.get(object19);
        if (iLookupThunk5 == object20) {
            __thunk__4__ = __site__4__.fault(object19);
            object20 = __thunk__4__.get(object19);
        }
        Object object21 = or__5238__auto__23668 = object20;
        if (object21 != null && object21 != Boolean.FALSE) {
            object2 = or__5238__auto__23668;
            or__5238__auto__23668 = null;
        } else {
            object2 = Boolean.FALSE;
        }
        Object ssl = object2;
        Object lockee__5436__auto__23669 = const__12.getRawRoot();
        Object object22 = provided_session;
        provided_session = null;
        Object object23 = ssl;
        ssl = null;
        Object object24 = port;
        port = null;
        Object object25 = endpoint;
        endpoint = null;
        Object object26 = lockee__5436__auto__23669;
        lockee__5436__auto__23669 = null;
        Object object27 = user;
        user = null;
        Object object28 = local_datacenter;
        local_datacenter = null;
        Object object29 = password;
        password = null;
        Object object30 = host;
        host = null;
        Object map__236572 = ((IFn)new kv_cassandra3$kv_cassandra$fn__23658(object22, object23, object24, object25, object26, object27, object28, object29, object30)).invoke();
        Object object31 = ((IFn)const__0.getRawRoot()).invoke(map__236572);
        if (object31 != null && object31 != Boolean.FALSE) {
            Object object32 = map__236572;
            map__236572 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object32)));
        } else {
            object = map__236572;
            map__236572 = null;
        }
        Object object33 = map__23657 = object;
        map__23657 = null;
        Object object34 = session = RT.get((Object)object33, (Object)const__5);
        session = null;
        Object object35 = table;
        table = null;
        return new KVCassandra3(object34, object35);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_cassandra3$kv_cassandra.invokeStatic(object2);
    }
}

