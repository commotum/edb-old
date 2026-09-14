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
import datomic.kv_cassandra$kv_cassandra$fn__17780;
import datomic.kv_cassandra.KVCassandra;

public final class kv_cassandra$kv_cassandra
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"host");
    public static final Keyword const__4 = RT.keyword(null, (String)"cluster");
    public static final Keyword const__5 = RT.keyword(null, (String)"table");
    public static final Keyword const__6 = RT.keyword(null, (String)"cluster-callback");
    public static final Object const__8 = 9042L;
    public static final Var const__12 = RT.var((String)"datomic.kv-cassandra", (String)"cluster-sessions");
    public static final Keyword const__13 = RT.keyword(null, (String)"session");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"port"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"user"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"password"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"ssl"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object endpoint) {
        Object object;
        Object object2;
        Object or__5238__auto__17790;
        Object object3;
        Object or__5238__auto__17789;
        Object object4;
        Object or__5238__auto__17788;
        Object object5;
        Object or__5238__auto__17787;
        Object object6;
        Object map__17778 = endpoint;
        Object object7 = ((IFn)const__0.getRawRoot()).invoke(map__17778);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = map__17778;
            map__17778 = null;
            object6 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object8)));
        } else {
            object6 = map__17778;
            map__17778 = null;
        }
        Object map__177782 = object6;
        Object host = RT.get((Object)map__177782, (Object)const__3);
        Object provided_cluster = RT.get((Object)map__177782, (Object)const__4);
        Object table = RT.get((Object)map__177782, (Object)const__5);
        Object object9 = map__177782;
        map__177782 = null;
        RT.get((Object)object9, (Object)const__6);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object10 = endpoint;
        Object object11 = iLookupThunk.get(object10);
        if (iLookupThunk == object11) {
            __thunk__0__ = __site__0__.fault(object10);
            object11 = __thunk__0__.get(object10);
        }
        Object object12 = or__5238__auto__17787 = object11;
        if (object12 != null && object12 != Boolean.FALSE) {
            object5 = or__5238__auto__17787;
            or__5238__auto__17787 = null;
        } else {
            object5 = const__8;
        }
        Object port = object5;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object13 = endpoint;
        Object object14 = iLookupThunk2.get(object13);
        if (iLookupThunk2 == object14) {
            __thunk__1__ = __site__1__.fault(object13);
            object14 = __thunk__1__.get(object13);
        }
        Object object15 = or__5238__auto__17788 = object14;
        if (object15 != null && object15 != Boolean.FALSE) {
            object4 = or__5238__auto__17788;
            or__5238__auto__17788 = null;
        } else {
            object4 = "";
        }
        Object user = object4;
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object16 = endpoint;
        Object object17 = iLookupThunk3.get(object16);
        if (iLookupThunk3 == object17) {
            __thunk__2__ = __site__2__.fault(object16);
            object17 = __thunk__2__.get(object16);
        }
        Object object18 = or__5238__auto__17789 = object17;
        if (object18 != null && object18 != Boolean.FALSE) {
            object3 = or__5238__auto__17789;
            or__5238__auto__17789 = null;
        } else {
            object3 = "";
        }
        Object password = object3;
        ILookupThunk iLookupThunk4 = __thunk__3__;
        Object object19 = endpoint;
        Object object20 = iLookupThunk4.get(object19);
        if (iLookupThunk4 == object20) {
            __thunk__3__ = __site__3__.fault(object19);
            object20 = __thunk__3__.get(object19);
        }
        Object object21 = or__5238__auto__17790 = object20;
        if (object21 != null && object21 != Boolean.FALSE) {
            object2 = or__5238__auto__17790;
            or__5238__auto__17790 = null;
        } else {
            object2 = Boolean.FALSE;
        }
        Object ssl = object2;
        Object lockee__5436__auto__17791 = const__12.getRawRoot();
        Object object22 = ssl;
        ssl = null;
        Object object23 = provided_cluster;
        provided_cluster = null;
        Object object24 = host;
        host = null;
        Object object25 = endpoint;
        endpoint = null;
        Object object26 = password;
        password = null;
        Object object27 = user;
        user = null;
        Object object28 = port;
        port = null;
        Object object29 = lockee__5436__auto__17791;
        lockee__5436__auto__17791 = null;
        Object map__17779 = ((IFn)new kv_cassandra$kv_cassandra$fn__17780(object22, object23, object24, object25, object26, object27, object28, object29)).invoke();
        Object object30 = ((IFn)const__0.getRawRoot()).invoke(map__17779);
        if (object30 != null && object30 != Boolean.FALSE) {
            Object object31 = map__17779;
            map__17779 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object31)));
        } else {
            object = map__17779;
            map__17779 = null;
        }
        Object map__177792 = object;
        Object cluster2 = RT.get((Object)map__177792, (Object)const__4);
        Object object32 = map__177792;
        map__177792 = null;
        Object session = RT.get((Object)object32, (Object)const__13);
        Object object33 = cluster2;
        cluster2 = null;
        Object object34 = session;
        session = null;
        Object object35 = table;
        table = null;
        return new KVCassandra(object33, object34, object35);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_cassandra$kv_cassandra.invokeStatic(object2);
    }
}

