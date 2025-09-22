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
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class tools$get_index
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object p__21769) {
        v0 = p__21769;
        p__21769 = null;
        map__21770 = v0;
        v1 = ((IFn)tools$get_index.const__0.getRawRoot()).invoke(map__21770);
        if (v1 != null && v1 != Boolean.FALSE) {
            v2 = map__21770;
            map__21770 = null;
            v3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)tools$get_index.const__1.getRawRoot()).invoke(v2)));
        } else {
            v3 = map__21770;
            map__21770 = null;
        }
        map__21770 = v3;
        cluster = RT.get((Object)map__21770, (Object)tools$get_index.const__3);
        v4 = map__21770;
        map__21770 = null;
        v5 = olookup = RT.get((Object)v4, (Object)tools$get_index.const__4);
        olookup = null;
        v6 = tools$get_index.__thunk__0__;
        v7 = (IFn)tools$get_index.const__6.getRawRoot();
        v8 = cluster;
        if (Util.classOf((Object)v8) == tools$get_index.__cached_class__0) ** GOTO lbl25
        if (!(v8 instanceof ClusteredStore)) {
            v8 = v8;
            tools$get_index.__cached_class__0 = Util.classOf((Object)v8);
lbl25:
            // 2 sources

            v9 = cluster;
            cluster = null;
            v10 = tools$get_index.const__7.getRawRoot().invoke(v8, ((IFn)tools$get_index.const__8.getRawRoot()).invoke(v9));
        } else {
            v11 = cluster;
            cluster = null;
            v10 = ((ClusteredStore)v8).get_ref(((IFn)tools$get_index.const__8.getRawRoot()).invoke(v11));
        }
        v12 = v7.invoke(v10);
        v13 = v6.get(v12);
        if (v6 == v13) {
            tools$get_index.__thunk__0__ = tools$get_index.__site__0__.fault(v12);
            v13 = tools$get_index.__thunk__0__.get(v12);
        }
        return RT.get((Object)v5, (Object)v13);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$get_index.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq?");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__3 = RT.keyword(null, (String)"cluster");
        const__4 = RT.keyword(null, (String)"olookup");
        const__6 = RT.var((String)"clojure.core", (String)"deref");
        const__7 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__8 = RT.var((String)"datomic.index", (String)"index-ref-key-name");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__0__ = __site__0__;
    }
}

