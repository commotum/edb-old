/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException
 */
package datomic.kv_dynamo;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import datomic.kv_dynamo.KVDynamo$fn__20489;
import datomic.kv_dynamo.KVDynamo$fn__20491;
import datomic.kv_store.KVStore;

public final class KVDynamo
implements KVStore,
IType {
    public final Object client;
    public final Object table;
    public final Object prefix;
    public static final Keyword const__0 = RT.keyword(null, (String)"id");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"update");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__3 = RT.var((String)"datomic.kv-dynamo", (String)"key-path");
    public static final Keyword const__4 = RT.keyword(null, (String)"ensure");
    public static final Var const__5 = RT.var((String)"datomic.ddb", (String)"create-item");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__7 = RT.var((String)"datomic.ddb", (String)"put-item");
    public static final Keyword const__8 = RT.keyword(null, (String)"tableName");
    public static final Keyword const__9 = RT.keyword(null, (String)"item");
    public static final Keyword const__10 = RT.keyword(null, (String)"expected");
    public static final Var const__11 = RT.var((String)"datomic.kv-dynamo", (String)"expected-map");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__14 = RT.var((String)"datomic.ddb-values", (String)"*retry*");
    public static final Var const__15 = RT.var((String)"datomic.kv-store", (String)"*retry*");
    public static final Keyword const__16 = RT.keyword(null, (String)"ok");
    public static final Var const__17 = RT.var((String)"datomic.ddb", (String)"get-item");
    public static final Keyword const__18 = RT.keyword(null, (String)"consistentRead");
    public static final Keyword const__19 = RT.keyword(null, (String)"key");
    public static final Var const__20 = RT.var((String)"datomic.ddb", (String)"create-key");
    public static final Var const__21 = RT.var((String)"datomic.ddb", (String)"deitem");
    public static final Var const__22 = RT.var((String)"datomic.kv-dynamo", (String)"remove-prefix");
    public static final Var const__23 = RT.var((String)"datomic.ddb", (String)"delete-item");
    public static final Keyword const__24 = RT.keyword(null, (String)"returnValues");
    public static final Var const__25 = RT.var((String)"datomic.ddb-values", (String)"delete-value");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"ensure"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"ensure"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public KVDynamo(Object object, Object object2, Object object3) {
        this.client = object;
        this.table = object2;
        this.prefix = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"client"), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"prefix"));
    }

    public Object close() {
        return null;
    }

    public Object delete(Object key, Object consistent_QMARK_) {
        Object object = key;
        key = null;
        Object path2 = ((IFn)const__3.getRawRoot()).invoke(this.prefix, object);
        Object object2 = consistent_QMARK_;
        consistent_QMARK_ = null;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object[] objectArray = new Object[6];
            objectArray[0] = const__8;
            objectArray[1] = this.table;
            objectArray[2] = const__19;
            Object object3 = path2;
            path2 = null;
            objectArray[3] = ((IFn)const__20.getRawRoot()).invoke(object3);
            objectArray[4] = const__24;
            objectArray[5] = "NONE";
            ((IFn)const__23.getRawRoot()).invoke(this.client, (Object)RT.mapUniqueKeys((Object[])objectArray));
        } else {
            Object object4 = path2;
            path2 = null;
            ((IFn)const__25.getRawRoot()).invoke(this.client, this.table, object4);
        }
        return const__16;
    }

    public Object get(Object key, Object consistent_QMARK_) {
        Object object;
        Object ret;
        Object object2;
        Object object3 = key;
        key = null;
        Object path2 = ((IFn)const__3.getRawRoot()).invoke(this_.prefix, object3);
        Object object4 = consistent_QMARK_;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object temp__5457__auto__20495;
            ILookupThunk iLookupThunk = __thunk__3__;
            Object[] objectArray = new Object[6];
            objectArray[0] = const__8;
            objectArray[1] = this_.table;
            objectArray[2] = const__18;
            Object object5 = consistent_QMARK_;
            consistent_QMARK_ = null;
            objectArray[3] = object5;
            objectArray[4] = const__19;
            Object object6 = path2;
            path2 = null;
            objectArray[5] = ((IFn)const__20.getRawRoot()).invoke(object6);
            Object object7 = ((IFn)const__17.getRawRoot()).invoke(this_.client, (Object)RT.mapUniqueKeys((Object[])objectArray));
            Object object8 = iLookupThunk.get(object7);
            if (iLookupThunk == object8) {
                __thunk__3__ = __site__3__.fault(object7);
                object8 = __thunk__3__.get(object7);
            }
            Object object9 = temp__5457__auto__20495 = object8;
            if (object9 != null && object9 != Boolean.FALSE) {
                Object ret2;
                Object object10 = temp__5457__auto__20495;
                temp__5457__auto__20495 = null;
                Object object11 = ret2 = object10;
                ret2 = null;
                object2 = ((IFn)const__21.getRawRoot()).invoke(object11);
            } else {
                object2 = null;
            }
        } else {
            ((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke((Object)const__14, const__15.get()));
            Object object12 = path2;
            path2 = null;
            object2 = ((IFn)new KVDynamo$fn__20491(this_.client, object12, this_.table)).invoke();
        }
        Object G__20493 = ret = object2;
        ILookupThunk iLookupThunk = __thunk__4__;
        Object object13 = ret;
        ret = null;
        Object object14 = iLookupThunk.get(object13);
        if (iLookupThunk == object14) {
            __thunk__4__ = __site__4__.fault(object13);
            object14 = __thunk__4__.get(object13);
        }
        if (object14 != null && object14 != Boolean.FALSE) {
            Object object15 = G__20493;
            G__20493 = null;
            KVDynamo this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object15, (Object)const__0, ((IFn)const__2.getRawRoot()).invoke(const__22.getRawRoot(), this_.prefix));
        } else {
            object = G__20493;
            G__20493 = null;
        }
        return object;
    }

    public Object put(Object val_map) {
        Keyword keyword;
        try {
            Object val_map2;
            Object object;
            Object G__20488 = val_map;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object2 = val_map;
            val_map = null;
            Object object3 = iLookupThunk.get(object2);
            if (iLookupThunk == object3) {
                __thunk__0__ = __site__0__.fault(object2);
                object3 = __thunk__0__.get(object2);
            }
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = G__20488;
                G__20488 = null;
                object = ((IFn)const__1.getRawRoot()).invoke(object4, (Object)const__0, ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), this.prefix));
            } else {
                object = G__20488;
                val_map2 = null;
            }
            val_map2 = object;
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = val_map2;
            Object object6 = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object6) {
                __thunk__1__ = __site__1__.fault(object5);
                object6 = __thunk__1__.get(object5);
            }
            if (object6 != null && object6 != Boolean.FALSE) {
                Object item = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(val_map2, (Object)const__4));
                IFn iFn = (IFn)const__7.getRawRoot();
                Object[] objectArray = new Object[6];
                objectArray[0] = const__8;
                objectArray[1] = this.table;
                objectArray[2] = const__9;
                Object object7 = item;
                item = null;
                objectArray[3] = object7;
                objectArray[4] = const__10;
                IFn iFn2 = (IFn)const__11.getRawRoot();
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object8 = val_map2;
                val_map2 = null;
                Object object9 = iLookupThunk3.get(object8);
                if (iLookupThunk3 == object9) {
                    __thunk__2__ = __site__2__.fault(object8);
                    object9 = __thunk__2__.get(object8);
                }
                objectArray[5] = iFn2.invoke(object9);
                iFn.invoke(this.client, (Object)RT.mapUniqueKeys((Object[])objectArray));
            } else {
                ((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke((Object)const__14, const__15.get()));
                Object object10 = val_map2;
                val_map2 = null;
                ((IFn)new KVDynamo$fn__20489(this.client, object10, this.table)).invoke();
            }
            keyword = const__16;
        }
        catch (ConditionalCheckFailedException ex) {
            keyword = null;
        }
        return keyword;
    }
}

