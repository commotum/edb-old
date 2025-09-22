/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.kv_cassandra3;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.kv_cassandra3.KVCassandra3$fn__23647;
import datomic.kv_store.KVStore;

public final class KVCassandra3
implements KVStore,
IType {
    public final Object session;
    public final Object table;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
    public static final Keyword const__4 = RT.keyword(null, (String)"rev");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Keyword const__6 = RT.keyword(null, (String)"ensure");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__8 = RT.keyword(null, (String)"id2");
    public static final Keyword const__9 = RT.keyword(null, (String)"map");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Keyword const__12 = RT.keyword(null, (String)"val");
    public static final AFn const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"id"), null});
    public static final Var const__15 = RT.var((String)"datomic.cassandra-v4", (String)"cql-insert");
    public static final Var const__16 = RT.var((String)"datomic.kv-cassandra3", (String)"cql-keys");
    public static final Var const__17 = RT.var((String)"datomic.cassandra-v4", (String)"cql-update");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__20 = RT.var((String)"datomic.cassandra-values-v4", (String)"*retry*");
    public static final Var const__21 = RT.var((String)"datomic.kv-store", (String)"*retry*");
    public static final Keyword const__22 = RT.keyword(null, (String)"ok");
    public static final Var const__23 = RT.var((String)"datomic.cassandra-v4", (String)"cql-select");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"read-string");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__27 = RT.var((String)"datomic.cassandra-values-v4", (String)"get-value");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");
    public static final Var const__29 = RT.var((String)"datomic.cassandra-v4", (String)"cql-delete");
    public static final Var const__30 = RT.var((String)"datomic.cassandra-values-v4", (String)"delete-value");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"map"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"map"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public KVCassandra3(Object object, Object object2) {
        this.session = object;
        this.table = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"SyncCqlSession")})), (Object)Symbol.intern(null, (String)"table"));
    }

    public Object close() {
        return null;
    }

    public Object delete(Object key, Object consistent_QMARK_) {
        Object object = consistent_QMARK_;
        consistent_QMARK_ = null;
        if (object != null && object != Boolean.FALSE) {
            Object object2 = key;
            key = null;
            ((IFn)const__29.getRawRoot()).invoke(this.session, this.table, object2, const__16.getRawRoot());
        } else {
            Object object3 = key;
            key = null;
            ((IFn)const__30.getRawRoot()).invoke(this.session, this.table, object3);
        }
        return const__22;
    }

    public Object get(Object key, Object consistent_QMARK_) {
        Object object;
        Object object2 = consistent_QMARK_;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object temp__5457__auto__23652;
            Object object3 = key;
            key = null;
            Object object4 = consistent_QMARK_;
            consistent_QMARK_ = null;
            Object object5 = temp__5457__auto__23652 = ((IFn)const__23.getRawRoot()).invoke(this.session, this.table, object3, const__16.getRawRoot(), object4);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object ret;
                Object object6;
                Object and__5236__auto__23651;
                Object object7;
                Object temp__5455__auto__23650;
                Object object8 = temp__5457__auto__23652;
                temp__5457__auto__23652 = null;
                Object ret2 = object8;
                ILookupThunk iLookupThunk = __thunk__1__;
                Object object9 = ret2;
                Object object10 = iLookupThunk.get(object9);
                if (iLookupThunk == object10) {
                    __thunk__1__ = __site__1__.fault(object9);
                    object10 = __thunk__1__.get(object9);
                }
                Object object11 = temp__5455__auto__23650 = object10;
                if (object11 != null && object11 != Boolean.FALSE) {
                    Object object12 = temp__5455__auto__23650;
                    temp__5455__auto__23650 = null;
                    Object v = object12;
                    Object object13 = ret2;
                    ret2 = null;
                    Object object14 = v;
                    v = null;
                    object7 = ((IFn)const__24.getRawRoot()).invoke(object13, (Object)const__5, object14);
                } else {
                    object7 = ret2;
                    ret2 = null;
                }
                Object ret3 = object7;
                ILookupThunk iLookupThunk2 = __thunk__2__;
                Object object15 = ret3;
                Object object16 = iLookupThunk2.get(object15);
                if (iLookupThunk2 == object16) {
                    __thunk__2__ = __site__2__.fault(object15);
                    object16 = __thunk__2__.get(object15);
                }
                Object object17 = and__5236__auto__23651 = object16;
                if (object17 != null && object17 != Boolean.FALSE) {
                    IFn iFn = (IFn)const__25.getRawRoot();
                    ILookupThunk iLookupThunk3 = __thunk__3__;
                    Object object18 = ret3;
                    Object object19 = iLookupThunk3.get(object18);
                    if (iLookupThunk3 == object19) {
                        __thunk__3__ = __site__3__.fault(object18);
                        object19 = __thunk__3__.get(object18);
                    }
                    object6 = iFn.invoke(object19);
                } else {
                    object6 = and__5236__auto__23651;
                    and__5236__auto__23651 = null;
                }
                Object m = object6;
                Object object20 = ret3;
                ret3 = null;
                Object object21 = m;
                m = null;
                object = ret = ((IFn)const__26.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object20, (Object)const__9, (Object)const__12), object21);
                ret = null;
            } else {
                object = null;
            }
        } else {
            Object object22;
            ((IFn)const__18.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke((Object)const__20, const__21.get()));
            try {
                Object object23 = key;
                key = null;
                object22 = ((IFn)const__27.getRawRoot()).invoke(this.session, this.table, object23);
            }
            finally {
                ((IFn)const__28.getRawRoot()).invoke();
            }
            object = object22;
        }
        return object;
    }

    public Object put(Object p__23644) {
        Object object;
        Object object2;
        Object map__23646;
        Object object3;
        Object object4 = p__23644;
        p__23644 = null;
        Object map__236462 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__236462);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__236462;
            map__236462 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__236462;
            map__236462 = null;
        }
        Object v_map = map__23646 = object3;
        Object id = RT.get((Object)map__23646, (Object)const__3);
        Object rev = RT.get((Object)map__23646, (Object)const__4);
        Object v = RT.get((Object)map__23646, (Object)const__5);
        Object object7 = map__23646;
        map__23646 = null;
        Object ensure = RT.get((Object)object7, (Object)const__6);
        Object m = ((IFn)const__7.getRawRoot()).invoke(v_map, (Object)const__3, (Object)const__4, (Object)const__5, (Object)const__6);
        Object[] objectArray = new Object[8];
        objectArray[0] = const__8;
        Object object8 = id;
        id = null;
        objectArray[1] = object8;
        objectArray[2] = const__4;
        Object object9 = rev;
        rev = null;
        objectArray[3] = object9;
        objectArray[4] = const__9;
        Integer n = RT.count((Object)m);
        if (n != null && n != Boolean.FALSE) {
            Object object10 = m;
            m = null;
            object2 = ((IFn)const__11.getRawRoot()).invoke(object10);
        } else {
            object2 = null;
        }
        objectArray[5] = object2;
        objectArray[6] = const__12;
        Object object11 = v;
        v = null;
        objectArray[7] = object11;
        IPersistentMap val_map = RT.mapUniqueKeys((Object[])objectArray);
        Object object12 = ensure;
        if (object12 != null && object12 != Boolean.FALSE) {
            if (Util.equiv((Object)ensure, (Object)const__14)) {
                IPersistentMap iPersistentMap = val_map;
                val_map = null;
                object = ((IFn)const__15.getRawRoot()).invoke(this.session, this.table, const__16.getRawRoot(), (Object)iPersistentMap, (Object)Boolean.TRUE);
            } else {
                IFn iFn = (IFn)const__17.getRawRoot();
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object13 = ensure;
                ensure = null;
                Object object14 = iLookupThunk.get(object13);
                if (iLookupThunk == object14) {
                    __thunk__0__ = __site__0__.fault(object13);
                    object14 = __thunk__0__.get(object13);
                }
                IPersistentMap iPersistentMap = val_map;
                val_map = null;
                object = iFn.invoke(this.session, this.table, object14, const__16.getRawRoot(), (Object)iPersistentMap);
            }
        } else {
            ((IFn)const__18.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke((Object)const__20, const__21.get()));
            Object object15 = v_map;
            v_map = null;
            object = ((IFn)new KVCassandra3$fn__23647(this.session, object15, this.table)).invoke();
        }
        return object != null && object != Boolean.FALSE ? const__22 : null;
    }
}

