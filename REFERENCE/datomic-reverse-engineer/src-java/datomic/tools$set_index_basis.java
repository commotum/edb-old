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
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class tools$set_index_basis
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Keyword const__4;
    public static final Keyword const__5;
    public static final Var const__6;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final Var const__19;
    public static final Keyword const__20;
    public static final Keyword const__21;
    public static final Keyword const__23;
    public static final Var const__24;
    public static final Var const__25;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object uri, Object basisT) {
        v0 = uri;
        uri = null;
        map__21744 = ((IFn)tools$set_index_basis.const__0.getRawRoot()).invoke(v0);
        v1 = ((IFn)tools$set_index_basis.const__1.getRawRoot()).invoke(map__21744);
        if (v1 != null && v1 != Boolean.FALSE) {
            v2 = map__21744;
            map__21744 = null;
            v3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)tools$set_index_basis.const__2.getRawRoot()).invoke(v2)));
        } else {
            v3 = map__21744;
            map__21744 = null;
        }
        cr = map__21744 = v3;
        cluster = RT.get((Object)map__21744, (Object)tools$set_index_basis.const__4);
        v4 = map__21744;
        map__21744 = null;
        olookup = RT.get((Object)v4, (Object)tools$set_index_basis.const__5);
        v5 = cr;
        cr = null;
        map__21745 = ((IFn)tools$set_index_basis.const__6.getRawRoot()).invoke(v5);
        v6 = ((IFn)tools$set_index_basis.const__1.getRawRoot()).invoke(map__21745);
        if (v6 != null && v6 != Boolean.FALSE) {
            v7 = map__21745;
            map__21745 = null;
            v8 = PersistentHashMap.create((ISeq)((ISeq)((IFn)tools$set_index_basis.const__2.getRawRoot()).invoke(v7)));
        } else {
            v8 = map__21745;
            map__21745 = null;
        }
        map__21745 = v8;
        db = RT.get((Object)map__21745, (Object)tools$set_index_basis.const__7);
        v9 = map__21745;
        map__21745 = null;
        v10 = log = RT.get((Object)v9, (Object)tools$set_index_basis.const__8);
        log = null;
        tx = ((IFn)tools$set_index_basis.const__9.getRawRoot()).invoke(((IFn)tools$set_index_basis.const__10.getRawRoot()).invoke(v10, basisT, (Object)Numbers.inc((Object)basisT)));
        nextT = ((IFn)tools$set_index_basis.const__12.getRawRoot()).invoke(tx);
        refkey = ((IFn)tools$set_index_basis.const__13.getRawRoot()).invoke(cluster);
        v11 = tools$set_index_basis.__thunk__0__;
        v12 = (IFn)tools$set_index_basis.const__15.getRawRoot();
        v13 = cluster;
        if (Util.classOf((Object)v13) == tools$set_index_basis.__cached_class__0) ** GOTO lbl44
        if (!(v13 instanceof ClusteredStore)) {
            v13 = v13;
            tools$set_index_basis.__cached_class__0 = Util.classOf((Object)v13);
lbl44:
            // 2 sources

            v14 = tools$set_index_basis.const__16.getRawRoot().invoke(v13, refkey);
        } else {
            v14 = ((ClusteredStore)v13).get_ref(refkey);
        }
        v15 = v12.invoke(v14);
        v16 = v11.get(v15);
        if (v11 == v16) {
            tools$set_index_basis.__thunk__0__ = tools$set_index_basis.__site__0__.fault(v15);
            v16 = tools$set_index_basis.__thunk__0__.get(v15);
        }
        k = v16;
        v17 = tx;
        tx = null;
        v18 = and__5236__auto__21748 = v17;
        if (v18 != null && v18 != Boolean.FALSE) {
            v19 = and__5236__auto__21747 = k;
            if (v19 != null && v19 != Boolean.FALSE) {
                v20 = db;
                db = null;
                v21 = ((IFn)tools$set_index_basis.const__17.getRawRoot()).invoke(v20, basisT);
            } else {
                v21 = and__5236__auto__21747;
                and__5236__auto__21747 = null;
            }
        } else {
            v21 = and__5236__auto__21748;
            and__5236__auto__21748 = null;
        }
        if (v21 != null && v21 != Boolean.FALSE) {
            v22 = k;
            k = null;
            index = RT.get((Object)olookup, (Object)v22);
            v23 = olookup;
            olookup = null;
            v24 = index;
            index = null;
            v25 = nextT;
            nextT = null;
            uuid = ((IFn)tools$set_index_basis.const__18.getRawRoot()).invoke(cluster, v23, ((IFn)tools$set_index_basis.const__19.getRawRoot()).invoke(v24, (Object)tools$set_index_basis.const__20, basisT, (Object)tools$set_index_basis.const__21, v25));
            v26 = cluster;
            cluster = null;
            v27 = refkey;
            refkey = null;
            v28 = uuid;
            uuid = null;
            if (Util.equiv((Object)tools$set_index_basis.const__23, (Object)((IFn)tools$set_index_basis.const__15.getRawRoot()).invoke(((IFn)tools$set_index_basis.const__24.getRawRoot()).invoke(v26, v27, ((IFn)tools$set_index_basis.const__25.getRawRoot()).invoke(v28))))) {
                v29 = basisT;
                basisT = null;
            } else {
                v29 = null;
            }
        } else {
            v29 = null;
        }
        return v29;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return tools$set_index_basis.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.tools", (String)"connection-resources");
        const__1 = RT.var((String)"clojure.core", (String)"seq?");
        const__2 = RT.var((String)"clojure.core", (String)"seq");
        const__4 = RT.keyword(null, (String)"cluster");
        const__5 = RT.keyword(null, (String)"olookup");
        const__6 = RT.var((String)"datomic.tools", (String)"db-resources");
        const__7 = RT.keyword(null, (String)"db");
        const__8 = RT.keyword(null, (String)"log");
        const__9 = RT.var((String)"clojure.core", (String)"first");
        const__10 = RT.var((String)"datomic.api", (String)"tx-range");
        const__12 = RT.var((String)"datomic.tools", (String)"log-entry->next-t");
        const__13 = RT.var((String)"datomic.index", (String)"index-ref-key-name");
        const__15 = RT.var((String)"clojure.core", (String)"deref");
        const__16 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__17 = RT.var((String)"datomic.tools", (String)"index-has-t?");
        const__18 = RT.var((String)"datomic.index", (String)"write-object");
        const__19 = RT.var((String)"clojure.core", (String)"assoc");
        const__20 = RT.keyword(null, (String)"basisT");
        const__21 = RT.keyword(null, (String)"nextT");
        const__23 = RT.keyword(null, (String)"ok");
        const__24 = RT.var((String)"datomic.cluster", (String)"reset-ref");
        const__25 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__0__ = __site__0__;
    }
}

