/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
 */
package datomic.kv_mem;

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
import datomic.kv_mem.KVMem$put_when__23557;
import datomic.kv_store.KVStore;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public final class KVMem
implements KVStore,
IType {
    public final Object m;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
    public static final Keyword const__4 = RT.keyword(null, (String)"ensure");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__7 = RT.keyword(null, (String)"else");
    public static final Keyword const__9 = RT.keyword(null, (String)"ok");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public KVMem(Object object) {
        this.m = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"m")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ConcurrentMap")})));
    }

    public Object close() {
        return null;
    }

    public Object delete(Object key, Object consistent_QMARK_) {
        Object object = key;
        key = null;
        ((Map)this.m).remove(object);
        return const__9;
    }

    public Object get(Object key, Object consistent_QMARK_) {
        Object object = key;
        key = null;
        return ((Map)this.m).get(object);
    }

    public Object put(Object val_map) {
        Object object;
        Object object2;
        KVMem$put_when__23557 put_when = new KVMem$put_when__23557(this.m);
        Object map__23556 = val_map;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__23556);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__23556;
            map__23556 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object2 = map__23556;
            map__23556 = null;
        }
        Object map__235562 = object2;
        Object id = RT.get((Object)map__235562, (Object)const__3);
        Object object5 = map__235562;
        map__235562 = null;
        Object ensure = RT.get((Object)object5, (Object)const__4);
        Object object6 = val_map;
        val_map = null;
        Object val_map2 = ((IFn)const__5.getRawRoot()).invoke(object6, (Object)const__4);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = ensure;
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        if (object8 != null && object8 != Boolean.FALSE) {
            KVMem$put_when__23557 kVMem$put_when__23557 = put_when;
            put_when = null;
            Object object9 = id;
            id = null;
            Object object10 = val_map2;
            val_map2 = null;
            Object object11 = ensure;
            ensure = null;
            object = ((IFn)kVMem$put_when__23557).invoke(object9, object10, object11);
        } else {
            Keyword keyword = const__7;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object object12 = id;
                id = null;
                Object object13 = val_map2;
                val_map2 = null;
                object = Util.identical((Object)((ConcurrentMap)this.m).putIfAbsent(object12, object13), null) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object = null;
            }
        }
        return object != null && object != Boolean.FALSE ? const__9 : null;
    }
}

