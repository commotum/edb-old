/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.kv_sql;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.kv_sql.KVSql$fn__11577;
import datomic.kv_store.KVStore;
import java.nio.ByteBuffer;

public final class KVSql
implements KVStore,
IType {
    public final Object spec;
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
    public static final Var const__12 = RT.var((String)"datomic.io", (String)"alias-buf-bytes");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__15 = RT.var((String)"datomic.sql", (String)"update");
    public static final Keyword const__16 = RT.keyword(null, (String)"else");
    public static final Keyword const__17 = RT.keyword(null, (String)"ok");
    public static final Var const__18 = RT.var((String)"datomic.sql", (String)"select");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"read-string");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__22 = RT.var((String)"datomic.sql", (String)"delete");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"map"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"map"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public KVSql(Object object) {
        this.spec = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"spec"));
    }

    public Object close() {
        return null;
    }

    public Object delete(Object key, Object consistent_QMARK_) {
        Object object = key;
        key = null;
        ((IFn)const__22.getRawRoot()).invoke(this.spec, object);
        return const__17;
    }

    public Object get(Object key, Object consistent_QMARK_) {
        Object object;
        Object temp__5457__auto__11582;
        Object object2 = key;
        key = null;
        Object object3 = temp__5457__auto__11582 = ((IFn)const__18.getRawRoot()).invoke(this.spec, object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object ret;
            Object object4;
            Object and__5236__auto__11581;
            Object object5;
            Object temp__5455__auto__11580;
            Object object6 = temp__5457__auto__11582;
            temp__5457__auto__11582 = null;
            Object ret2 = object6;
            ILookupThunk iLookupThunk = __thunk__2__;
            Object object7 = ret2;
            Object object8 = iLookupThunk.get(object7);
            if (iLookupThunk == object8) {
                __thunk__2__ = __site__2__.fault(object7);
                object8 = __thunk__2__.get(object7);
            }
            Object object9 = temp__5455__auto__11580 = object8;
            if (object9 != null && object9 != Boolean.FALSE) {
                Object object10 = temp__5455__auto__11580;
                temp__5455__auto__11580 = null;
                Object v = object10;
                Object object11 = ret2;
                ret2 = null;
                Object object12 = v;
                v = null;
                object5 = ((IFn)const__19.getRawRoot()).invoke(object11, (Object)const__5, (Object)ByteBuffer.wrap((byte[])object12));
            } else {
                object5 = ret2;
                ret2 = null;
            }
            Object ret3 = object5;
            ILookupThunk iLookupThunk2 = __thunk__3__;
            Object object13 = ret3;
            Object object14 = iLookupThunk2.get(object13);
            if (iLookupThunk2 == object14) {
                __thunk__3__ = __site__3__.fault(object13);
                object14 = __thunk__3__.get(object13);
            }
            Object object15 = and__5236__auto__11581 = object14;
            if (object15 != null && object15 != Boolean.FALSE) {
                IFn iFn = (IFn)const__20.getRawRoot();
                ILookupThunk iLookupThunk3 = __thunk__4__;
                Object object16 = ret3;
                Object object17 = iLookupThunk3.get(object16);
                if (iLookupThunk3 == object17) {
                    __thunk__4__ = __site__4__.fault(object16);
                    object17 = __thunk__4__.get(object16);
                }
                object4 = iFn.invoke(object17);
            } else {
                object4 = and__5236__auto__11581;
                and__5236__auto__11581 = null;
            }
            Object m = object4;
            Object object18 = ret3;
            ret3 = null;
            Object object19 = m;
            m = null;
            object = ret = ((IFn)const__21.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object18, (Object)const__8, (Object)const__11), object19);
            ret = null;
        } else {
            object = null;
        }
        return object;
    }

    public Object put(Object v_map) {
        Object object;
        Object object2;
        Object object3;
        Object object4;
        Object map__11576 = v_map;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__11576);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__11576;
            map__11576 = null;
            object4 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object4 = map__11576;
            map__11576 = null;
        }
        Object map__115762 = object4;
        Object id = RT.get((Object)map__115762, (Object)const__3);
        Object rev = RT.get((Object)map__115762, (Object)const__4);
        Object v = RT.get((Object)map__115762, (Object)const__5);
        Object object7 = map__115762;
        map__115762 = null;
        Object ensure = RT.get((Object)object7, (Object)const__6);
        Object object8 = v_map;
        v_map = null;
        Object m = ((IFn)const__7.getRawRoot()).invoke(object8, (Object)const__3, (Object)const__4, (Object)const__5, (Object)const__6);
        Object[] objectArray = new Object[8];
        objectArray[0] = const__3;
        objectArray[1] = id;
        objectArray[2] = const__4;
        Object object9 = rev;
        rev = null;
        objectArray[3] = object9;
        objectArray[4] = const__8;
        Integer n = RT.count((Object)m);
        if (n != null && n != Boolean.FALSE) {
            Object object10 = m;
            m = null;
            object3 = ((IFn)const__10.getRawRoot()).invoke(object10);
        } else {
            object3 = null;
        }
        objectArray[5] = object3;
        objectArray[6] = const__11;
        Object object11 = v;
        if (object11 != null && object11 != Boolean.FALSE) {
            Object object12 = v;
            v = null;
            object2 = ((IFn)const__12.getRawRoot()).invoke(object12);
        } else {
            object2 = null;
        }
        objectArray[7] = object2;
        IPersistentMap val_map = RT.mapUniqueKeys((Object[])objectArray);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object13 = ensure;
        Object object14 = iLookupThunk.get(object13);
        if (iLookupThunk == object14) {
            __thunk__0__ = __site__0__.fault(object13);
            object14 = __thunk__0__.get(object13);
        }
        if (object14 != null && object14 != Boolean.FALSE) {
            IFn iFn = (IFn)const__13.getRawRoot();
            IFn iFn2 = (IFn)const__15.getRawRoot();
            Object object15 = id;
            id = null;
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object16 = ensure;
            ensure = null;
            Object object17 = iLookupThunk2.get(object16);
            if (iLookupThunk2 == object17) {
                __thunk__1__ = __site__1__.fault(object16);
                object17 = __thunk__1__.get(object16);
            }
            IPersistentMap iPersistentMap = val_map;
            val_map = null;
            object = iFn.invoke((Object)(Numbers.isZero((Object)iFn2.invoke(this.spec, object15, object17, ((IFn)const__7.getRawRoot()).invoke((Object)iPersistentMap, (Object)const__3))) ? Boolean.TRUE : Boolean.FALSE));
        } else {
            Keyword keyword = const__16;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object object18 = id;
                id = null;
                IPersistentMap iPersistentMap = val_map;
                val_map = null;
                object = ((IFn)new KVSql$fn__11577(object18, this.spec, iPersistentMap)).invoke();
            } else {
                object = null;
            }
        }
        return object != null && object != Boolean.FALSE ? const__17 : null;
    }
}

