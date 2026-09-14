/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
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
package datomic.kv_cassandra;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
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
import datomic.kv_store.KVStore;

public final class KVCassandra
implements KVStore,
IType {
    public final Object cluster;
    public final Object session;
    public final Object table;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
    public static final Keyword const__4 = RT.keyword(null, (String)"rev");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Keyword const__6 = RT.keyword(null, (String)"ensure");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__8 = RT.keyword(null, (String)"map");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Keyword const__11 = RT.keyword(null, (String)"val");
    public static final AFn const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"id"), null});
    public static final Var const__14 = RT.var((String)"datomic.cassandra", (String)"cql-insert");
    public static final Var const__15 = RT.var((String)"datomic.kv-cassandra", (String)"cql-keys");
    public static final Var const__16 = RT.var((String)"datomic.cassandra", (String)"cql-update");
    public static final Keyword const__17 = RT.keyword(null, (String)"ok");
    public static final Var const__18 = RT.var((String)"datomic.cassandra", (String)"cql-select");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"read-string");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__22 = RT.var((String)"datomic.cassandra", (String)"cql-delete");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"map"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"map"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public KVCassandra(Object object, Object object2, Object object3) {
        this.cluster = object;
        this.session = object2;
        this.table = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"session"), (Object)Symbol.intern(null, (String)"table"));
    }

    public Object close() {
        return null;
    }

    public Object delete(Object key, Object consistent_QMARK_) {
        Object object = key;
        key = null;
        ((IFn)const__22.getRawRoot()).invoke(this.session, this.table, object, const__15.getRawRoot());
        return const__17;
    }

    public Object get(Object key, Object consistent_QMARK_) {
        Object object;
        Object temp__5457__auto__17774;
        Object object2 = key;
        key = null;
        Object object3 = consistent_QMARK_;
        consistent_QMARK_ = null;
        Object object4 = temp__5457__auto__17774 = ((IFn)const__18.getRawRoot()).invoke(this.session, this.table, object2, const__15.getRawRoot(), object3);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object ret;
            Object object5;
            Object and__5236__auto__17773;
            Object object6;
            Object temp__5455__auto__17772;
            Object object7 = temp__5457__auto__17774;
            temp__5457__auto__17774 = null;
            Object ret2 = object7;
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object8 = ret2;
            Object object9 = iLookupThunk.get(object8);
            if (iLookupThunk == object9) {
                __thunk__1__ = __site__1__.fault(object8);
                object9 = __thunk__1__.get(object8);
            }
            Object object10 = temp__5455__auto__17772 = object9;
            if (object10 != null && object10 != Boolean.FALSE) {
                Object object11 = temp__5455__auto__17772;
                temp__5455__auto__17772 = null;
                Object v = object11;
                Object object12 = ret2;
                ret2 = null;
                Object object13 = v;
                v = null;
                object6 = ((IFn)const__19.getRawRoot()).invoke(object12, (Object)const__5, object13);
            } else {
                object6 = ret2;
                ret2 = null;
            }
            Object ret3 = object6;
            ILookupThunk iLookupThunk2 = __thunk__2__;
            Object object14 = ret3;
            Object object15 = iLookupThunk2.get(object14);
            if (iLookupThunk2 == object15) {
                __thunk__2__ = __site__2__.fault(object14);
                object15 = __thunk__2__.get(object14);
            }
            Object object16 = and__5236__auto__17773 = object15;
            if (object16 != null && object16 != Boolean.FALSE) {
                IFn iFn = (IFn)const__20.getRawRoot();
                ILookupThunk iLookupThunk3 = __thunk__3__;
                Object object17 = ret3;
                Object object18 = iLookupThunk3.get(object17);
                if (iLookupThunk3 == object18) {
                    __thunk__3__ = __site__3__.fault(object17);
                    object18 = __thunk__3__.get(object17);
                }
                object5 = iFn.invoke(object18);
            } else {
                object5 = and__5236__auto__17773;
                and__5236__auto__17773 = null;
            }
            Object m = object5;
            Object object19 = ret3;
            ret3 = null;
            Object object20 = m;
            m = null;
            object = ret = ((IFn)const__21.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object19, (Object)const__8, (Object)const__11), object20);
            ret = null;
        } else {
            object = null;
        }
        return object;
    }

    public Object put(Object p__17768) {
        Object object;
        Object object2;
        Object map__17770;
        Object object3;
        Object object4 = p__17768;
        p__17768 = null;
        Object map__177702 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__177702);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__177702;
            map__177702 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__177702;
            map__177702 = null;
        }
        Object v_map = map__17770 = object3;
        Object id = RT.get((Object)map__17770, (Object)const__3);
        Object rev = RT.get((Object)map__17770, (Object)const__4);
        Object v = RT.get((Object)map__17770, (Object)const__5);
        Object object7 = map__17770;
        map__17770 = null;
        Object ensure = RT.get((Object)object7, (Object)const__6);
        Object object8 = v_map;
        v_map = null;
        Object m = ((IFn)const__7.getRawRoot()).invoke(object8, (Object)const__3, (Object)const__4, (Object)const__5, (Object)const__6);
        Object[] objectArray = new Object[8];
        objectArray[0] = const__3;
        Object object9 = id;
        id = null;
        objectArray[1] = object9;
        objectArray[2] = const__4;
        Object object10 = rev;
        rev = null;
        objectArray[3] = object10;
        objectArray[4] = const__8;
        Integer n = RT.count((Object)m);
        if (n != null && n != Boolean.FALSE) {
            Object object11 = m;
            m = null;
            object2 = ((IFn)const__10.getRawRoot()).invoke(object11);
        } else {
            object2 = null;
        }
        objectArray[5] = object2;
        objectArray[6] = const__11;
        Object object12 = v;
        v = null;
        objectArray[7] = object12;
        IPersistentMap val_map = RT.mapUniqueKeys((Object[])objectArray);
        Object object13 = ensure;
        if (object13 != null && object13 != Boolean.FALSE) {
            if (Util.equiv((Object)ensure, (Object)const__13)) {
                IPersistentMap iPersistentMap = val_map;
                val_map = null;
                object = ((IFn)const__14.getRawRoot()).invoke(this.session, this.table, const__15.getRawRoot(), (Object)iPersistentMap, (Object)Boolean.TRUE);
            } else {
                IFn iFn = (IFn)const__16.getRawRoot();
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object14 = ensure;
                ensure = null;
                Object object15 = iLookupThunk.get(object14);
                if (iLookupThunk == object15) {
                    __thunk__0__ = __site__0__.fault(object14);
                    object15 = __thunk__0__.get(object14);
                }
                IPersistentMap iPersistentMap = val_map;
                val_map = null;
                object = iFn.invoke(this.session, this.table, object15, const__15.getRawRoot(), (Object)iPersistentMap);
            }
        } else {
            IPersistentMap iPersistentMap = val_map;
            val_map = null;
            object = ((IFn)const__14.getRawRoot()).invoke(this.session, this.table, const__15.getRawRoot(), (Object)iPersistentMap, (Object)Boolean.FALSE);
        }
        return object != null && object != Boolean.FALSE ? const__17 : null;
    }
}

