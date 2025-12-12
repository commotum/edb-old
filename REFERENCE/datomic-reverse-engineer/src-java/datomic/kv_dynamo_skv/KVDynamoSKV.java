/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
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
 *  com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
 */
package datomic.kv_dynamo_skv;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
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
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import datomic.kv_store.KVStore;
import datomic.simple_kv.KV;

public final class KVDynamoSKV
implements KVStore,
IType {
    public final Object client;
    public final Object table;
    public final Object skv;
    public final Object prefix;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Keyword const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Keyword const__11;
    public static final Keyword const__12;
    public static final Keyword const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Keyword const__18;
    public static final Var const__19;
    public static final Keyword const__20;
    public static final Keyword const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final Var const__27;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;

    public KVDynamoSKV(Object object, Object object2, Object object3, Object object4) {
        this.client = object;
        this.table = object2;
        this.skv = object3;
        this.prefix = object4;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"client"), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"skv"), (Object)Symbol.intern(null, (String)"prefix"));
    }

    public Object close() {
        return null;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object delete(Object key, Object consistent_QMARK_) {
        Object object;
        KVDynamoSKV this_;
        Object object2 = consistent_QMARK_;
        consistent_QMARK_ = null;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = key;
            key = null;
            this_ = null;
            object = ((IFn)const__26.getRawRoot()).invoke(this_.client, this_.table, ((IFn)const__9.getRawRoot()).invoke(this_.prefix, (Object)"/", object3));
            return object;
        }
        Object object4 = this_.skv;
        if (Util.classOf((Object)object4) != __cached_class__1) {
            if (object4 instanceof KV) {
                Object object5 = key;
                key = null;
                object = ((KV)object4).delete(object5);
                return object;
            }
            object4 = object4;
            __cached_class__1 = Util.classOf((Object)object4);
        }
        Object object6 = key;
        key = null;
        this_ = null;
        object = const__27.getRawRoot().invoke(object4, object6);
        return object;
    }

    public Object get(Object key, Object consistent_QMARK_) {
        Object object;
        KVDynamoSKV this_;
        Object object2 = consistent_QMARK_;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object temp__5457__auto__23544;
            ILookupThunk iLookupThunk = __thunk__2__;
            Object[] objectArray = new Object[6];
            objectArray[0] = const__11;
            objectArray[1] = this_.table;
            objectArray[2] = const__20;
            Object object3 = consistent_QMARK_;
            consistent_QMARK_ = null;
            objectArray[3] = object3;
            objectArray[4] = const__21;
            objectArray[5] = ((IFn)const__22.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(this_.prefix, (Object)"/", key));
            Object object4 = ((IFn)const__19.getRawRoot()).invoke(this_.client, (Object)RT.mapUniqueKeys((Object[])objectArray));
            Object object5 = iLookupThunk.get(object4);
            if (iLookupThunk == object5) {
                __thunk__2__ = __site__2__.fault(object4);
                object5 = __thunk__2__.get(object4);
            }
            Object object6 = temp__5457__auto__23544 = object5;
            if (object6 != null && object6 != Boolean.FALSE) {
                Object ret;
                Object object7 = temp__5457__auto__23544;
                temp__5457__auto__23544 = null;
                Object object8 = ret = object7;
                ret = null;
                Object object9 = key;
                key = null;
                this_ = null;
                object = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke(object8), (Object)const__3, object9);
            } else {
                object = null;
            }
        } else {
            Object temp__5457__auto__23545;
            Object object10 = temp__5457__auto__23545 = ((IFn)const__24.getRawRoot()).invoke(this_.skv, key);
            if (object10 != null && object10 != Boolean.FALSE) {
                Object object11 = temp__5457__auto__23545;
                temp__5457__auto__23545 = null;
                Object ret = object11;
                Object object12 = key;
                key = null;
                Object object13 = ret;
                ret = null;
                this_ = null;
                object = ((IFn)const__25.getRawRoot()).invoke(object12, object13);
            } else {
                object = null;
            }
        }
        return object;
    }

    /*
     * Unable to fully structure code
     */
    public Object put(Object p__23540) {
        v0 = p__23540;
        p__23540 = null;
        map__23542 = v0;
        v1 = ((IFn)KVDynamoSKV.const__0.getRawRoot()).invoke(map__23542);
        if (v1 != null && v1 != Boolean.FALSE) {
            v2 = map__23542;
            map__23542 = null;
            v3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)KVDynamoSKV.const__1.getRawRoot()).invoke(v2)));
        } else {
            v3 = map__23542;
            map__23542 = null;
        }
        val_map = map__23542 = v3;
        id = RT.get((Object)map__23542, (Object)KVDynamoSKV.const__3);
        v = RT.get((Object)map__23542, (Object)KVDynamoSKV.const__4);
        v4 = map__23542;
        map__23542 = null;
        RT.get((Object)v4, (Object)KVDynamoSKV.const__5);
        try {
            block13: {
                block12: {
                    v5 = KVDynamoSKV.__thunk__0__;
                    v6 = val_map;
                    v7 = v5.get(v6);
                    if (v5 == v7) {
                        KVDynamoSKV.__thunk__0__ = KVDynamoSKV.__site__0__.fault(v6);
                        v7 = KVDynamoSKV.__thunk__0__.get(v6);
                    }
                    if (v7 == null || v7 == Boolean.FALSE) break block12;
                    v8 = id;
                    id = null;
                    item = ((IFn)KVDynamoSKV.const__6.getRawRoot()).invoke(((IFn)KVDynamoSKV.const__7.getRawRoot()).invoke(((IFn)KVDynamoSKV.const__8.getRawRoot()).invoke(val_map, (Object)KVDynamoSKV.const__3, ((IFn)KVDynamoSKV.const__9.getRawRoot()).invoke(this.prefix, (Object)"/", v8)), (Object)KVDynamoSKV.const__5));
                    v9 = (IFn)KVDynamoSKV.const__10.getRawRoot();
                    v10 = new Object[6];
                    v10[0] = KVDynamoSKV.const__11;
                    v10[1] = this.table;
                    v10[2] = KVDynamoSKV.const__12;
                    v11 = item;
                    item = null;
                    v10[3] = v11;
                    v10[4] = KVDynamoSKV.const__13;
                    v12 = (IFn)KVDynamoSKV.const__14.getRawRoot();
                    v13 = KVDynamoSKV.__thunk__1__;
                    v14 = val_map;
                    val_map = null;
                    v15 = v13.get(v14);
                    if (v13 == v15) {
                        KVDynamoSKV.__thunk__1__ = KVDynamoSKV.__site__1__.fault(v14);
                        v15 = KVDynamoSKV.__thunk__1__.get(v14);
                    }
                    v10[5] = v12.invoke(v15);
                    v9.invoke(this.client, (Object)RT.mapUniqueKeys((Object[])v10));
                    break block13;
                }
                v16 = val_map;
                val_map = null;
                m = ((IFn)KVDynamoSKV.const__7.getRawRoot()).invoke(v16, (Object)KVDynamoSKV.const__3, (Object)KVDynamoSKV.const__4, (Object)KVDynamoSKV.const__5);
                v17 = this.skv;
                if (Util.classOf((Object)v17) == KVDynamoSKV.__cached_class__0) ** GOTO lbl62
                if (!(v17 instanceof KV)) {
                    v17 = v17;
                    KVDynamoSKV.__cached_class__0 = Util.classOf((Object)v17);
lbl62:
                    // 2 sources

                    v18 = id;
                    id = null;
                    v19 = ((IFn)KVDynamoSKV.const__16.getRawRoot()).invoke(m);
                    if (v19 != null && v19 != Boolean.FALSE) {
                        v20 = v;
                        v = null;
                    } else {
                        v21 = m;
                        m = null;
                        v22 = v;
                        v = null;
                        v20 = ((IFn)KVDynamoSKV.const__17.getRawRoot()).invoke(v21, v22);
                    }
                    v23 = KVDynamoSKV.const__15.getRawRoot().invoke(v17, v18, v20);
                } else {
                    v24 = (KV)v17;
                    v25 = id;
                    id = null;
                    v26 = ((IFn)KVDynamoSKV.const__16.getRawRoot()).invoke(m);
                    if (v26 != null && v26 != Boolean.FALSE) {
                        v27 = v;
                        v = null;
                    } else {
                        v28 = m;
                        m = null;
                        v29 = v;
                        v = null;
                        v27 = ((IFn)KVDynamoSKV.const__17.getRawRoot()).invoke(v28, v29);
                    }
                    v23 = v24.put(v25, v27);
                }
            }
            var8_10 = KVDynamoSKV.const__18;
        }
        catch (ConditionalCheckFailedException ex) {
            var8_10 = null;
        }
        return var8_10;
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq?");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__3 = RT.keyword(null, (String)"id");
        const__4 = RT.keyword(null, (String)"v");
        const__5 = RT.keyword(null, (String)"ensure");
        const__6 = RT.var((String)"datomic.ddb", (String)"create-item");
        const__7 = RT.var((String)"clojure.core", (String)"dissoc");
        const__8 = RT.var((String)"clojure.core", (String)"assoc");
        const__9 = RT.var((String)"clojure.core", (String)"str");
        const__10 = RT.var((String)"datomic.ddb", (String)"put-item");
        const__11 = RT.keyword(null, (String)"tableName");
        const__12 = RT.keyword(null, (String)"item");
        const__13 = RT.keyword(null, (String)"expected");
        const__14 = RT.var((String)"datomic.kv-dynamo-skv", (String)"expected-map");
        const__15 = RT.var((String)"datomic.simple-kv", (String)"put");
        const__16 = RT.var((String)"clojure.core", (String)"empty?");
        const__17 = RT.var((String)"datomic.simple-kv", (String)"pack");
        const__18 = RT.keyword(null, (String)"ok");
        const__19 = RT.var((String)"datomic.ddb", (String)"get-item");
        const__20 = RT.keyword(null, (String)"consistentRead");
        const__21 = RT.keyword(null, (String)"key");
        const__22 = RT.var((String)"datomic.ddb", (String)"create-key");
        const__23 = RT.var((String)"datomic.ddb", (String)"deitem");
        const__24 = RT.var((String)"datomic.simple-kv", (String)"get-with-retry");
        const__25 = RT.var((String)"datomic.simple-kv", (String)"unpack");
        const__26 = RT.var((String)"datomic.ddb-values", (String)"delete-value");
        const__27 = RT.var((String)"datomic.simple-kv", (String)"delete");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"ensure"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"ensure"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
        __thunk__2__ = __site__2__;
    }
}

