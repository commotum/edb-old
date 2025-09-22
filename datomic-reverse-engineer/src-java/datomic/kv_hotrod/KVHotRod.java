/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
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
 *  org.infinispan.client.hotrod.Flag
 *  org.infinispan.client.hotrod.RemoteCache
 */
package datomic.kv_hotrod;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
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
import datomic.kv_hotrod.KVHotRod$put_when__17803;
import datomic.kv_store.KVStore;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;

public final class KVHotRod
implements KVStore,
IType {
    public final Object cache;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
    public static final Keyword const__4 = RT.keyword(null, (String)"ensure");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__6 = RT.keyword(null, (String)"v");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__8 = RT.var((String)"datomic.io", (String)"alias-buf-bytes");
    public static final AFn const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"id"), null});
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__13 = RT.var((String)"datomic.kv-hotrod", (String)"FORCE");
    public static final Keyword const__14 = RT.keyword(null, (String)"else");
    public static final Keyword const__15 = RT.keyword(null, (String)"ok");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"v"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"v"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public KVHotRod(Object object) {
        this.cache = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"cache")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"org.infinispan.client.hotrod.RemoteCache")})));
    }

    public Object close() {
        return null;
    }

    public Object delete(Object key, Object consistent_QMARK_) {
        Object object = key;
        key = null;
        ((Map)this.cache).remove(object);
        return const__15;
    }

    public Object get(Object key, Object consistent_QMARK_) {
        Object v11;
        Object temp__5457__auto__17809;
        Object object = key;
        key = null;
        Object v = temp__5457__auto__17809 = ((Map)this.cache).get(object);
        if (v != null && v != Boolean.FALSE) {
            Object object2;
            Object temp__5455__auto__17808;
            Object v2 = temp__5457__auto__17809;
            temp__5457__auto__17809 = null;
            Object ret = v2;
            ILookupThunk iLookupThunk = __thunk__1__;
            Object v3 = ret;
            Object object3 = iLookupThunk.get(v3);
            if (iLookupThunk == object3) {
                __thunk__1__ = __site__1__.fault(v3);
                object3 = __thunk__1__.get(v3);
            }
            Object object4 = temp__5455__auto__17808 = object3;
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = temp__5455__auto__17808;
                temp__5455__auto__17808 = null;
                Object v4 = object5;
                Object v5 = ret;
                ret = null;
                Object object6 = v4;
                v4 = null;
                object2 = ((IFn)const__7.getRawRoot()).invoke(v5, (Object)const__6, (Object)ByteBuffer.wrap((byte[])object6));
            } else {
                object2 = ret;
                ret = null;
            }
            Object ret2 = object2;
            v11 = ret2;
            ret2 = null;
        } else {
            v11 = null;
        }
        return v11;
    }

    public Object put(Object val_map) {
        Object object;
        Object object2;
        Object v;
        Object object3;
        KVHotRod$put_when__17803 put_when = new KVHotRod$put_when__17803(this.cache);
        Object map__17802 = val_map;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__17802);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__17802;
            map__17802 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object3 = map__17802;
            map__17802 = null;
        }
        Object map__178022 = object3;
        Object id = RT.get((Object)map__178022, (Object)const__3);
        Object object6 = map__178022;
        map__178022 = null;
        Object ensure = RT.get((Object)object6, (Object)const__4);
        Object object7 = val_map;
        val_map = null;
        Object val_map2 = ((IFn)const__5.getRawRoot()).invoke(object7, (Object)const__4);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object8 = val_map2;
        Object object9 = iLookupThunk.get(object8);
        if (iLookupThunk == object9) {
            __thunk__0__ = __site__0__.fault(object8);
            object9 = __thunk__0__.get(object8);
        }
        Object object10 = v = object9;
        if (object10 != null && object10 != Boolean.FALSE) {
            Object object11 = val_map2;
            val_map2 = null;
            Object object12 = v;
            v = null;
            object2 = ((IFn)const__7.getRawRoot()).invoke(object11, (Object)const__6, ((IFn)const__8.getRawRoot()).invoke(object12));
        } else {
            object2 = val_map2;
            val_map2 = null;
        }
        Object val_map3 = object2;
        if (Util.equiv((Object)ensure, (Object)const__10)) {
            Object object13 = id;
            id = null;
            Object object14 = val_map3;
            val_map3 = null;
            object = Util.identical((Object)((ConcurrentMap)((RemoteCache)this.cache).withFlags((Flag[])((IFn)const__12.getRawRoot()).invoke(const__13.getRawRoot()))).putIfAbsent(object13, object14), null) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            Object object15 = ensure;
            if (object15 != null && object15 != Boolean.FALSE) {
                KVHotRod$put_when__17803 kVHotRod$put_when__17803 = put_when;
                put_when = null;
                Object object16 = id;
                id = null;
                Object object17 = val_map3;
                val_map3 = null;
                Object object18 = ensure;
                ensure = null;
                object = ((IFn)kVHotRod$put_when__17803).invoke(object16, object17, object18);
            } else {
                Keyword keyword = const__14;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object object19 = id;
                    id = null;
                    Object object20 = val_map3;
                    val_map3 = null;
                    ((Map)this.cache).put(object19, object20);
                    object = Boolean.TRUE;
                } else {
                    object = null;
                }
            }
        }
        return object != null && object != Boolean.FALSE ? const__15 : null;
    }
}

