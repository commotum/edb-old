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
 *  org.slf4j.LoggerFactory
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
import datomic.cluster$reset_ref$fn__10625;
import datomic.cluster.ClusteredStore;
import org.slf4j.LoggerFactory;

public final class cluster$reset_ref
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Keyword const__5;
    public static final Keyword const__6;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Keyword const__9;
    public static final Keyword const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;
    public static final Var const__16;
    public static final Var const__17;
    public static final Keyword const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Keyword const__23;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;
    static final KeywordLookupSite __site__4__;
    static ILookupThunk __thunk__4__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cluster, Object k, Object v) {
        v0 = (IFn)cluster$reset_ref.const__0.getRawRoot();
        v1 = cluster;
        if (Util.classOf((Object)v1) == cluster$reset_ref.__cached_class__0) ** GOTO lbl7
        if (!(v1 instanceof ClusteredStore)) {
            v1 = v1;
            cluster$reset_ref.__cached_class__0 = Util.classOf((Object)v1);
lbl7:
            // 2 sources

            v2 = cluster$reset_ref.const__1.getRawRoot().invoke(v1, k);
        } else {
            v2 = ((ClusteredStore)v1).get_ref(k);
        }
        map__10621 = v0.invoke(v2);
        v3 = ((IFn)cluster$reset_ref.const__2.getRawRoot()).invoke(map__10621);
        if (v3 != null && v3 != Boolean.FALSE) {
            v4 = map__10621;
            map__10621 = null;
            v5 = PersistentHashMap.create((ISeq)((ISeq)((IFn)cluster$reset_ref.const__3.getRawRoot()).invoke(v4)));
        } else {
            v5 = map__10621;
            map__10621 = null;
        }
        prev = map__10621 = v5;
        v6 = map__10621;
        map__10621 = null;
        rev = RT.get((Object)v6, (Object)cluster$reset_ref.const__5);
        v7 = new Object[8];
        v7[0] = cluster$reset_ref.const__6;
        v7[1] = cluster$reset_ref.const__7;
        v7[2] = cluster$reset_ref.const__8;
        v7[3] = k;
        v7[4] = cluster$reset_ref.const__9;
        v8 = cluster$reset_ref.__thunk__0__;
        v9 = prev;
        prev = null;
        v10 = v8.get(v9);
        if (v8 == v10) {
            cluster$reset_ref.__thunk__0__ = cluster$reset_ref.__site__0__.fault(v9);
            v10 = cluster$reset_ref.__thunk__0__.get(v9);
        }
        v7[5] = v10;
        v7[6] = cluster$reset_ref.const__10;
        v7[7] = v;
        m_10622 = RT.mapUniqueKeys((Object[])v7);
        logger = LoggerFactory.getLogger((String)"datomic.cluster");
        if (logger.isInfoEnabled()) {
            v11 = logger;
            logger = null;
            v11.info((String)((IFn)cluster$reset_ref.const__11.getRawRoot()).invoke(((IFn)cluster$reset_ref.const__12.getRawRoot()).invoke((Object)m_10622, (Object)cluster$reset_ref.const__13, (Object)cluster$reset_ref.const__14)));
        }
        start__8981__auto__10630 = System.nanoTime();
        v12 = rev;
        rev = null;
        v13 = k;
        k = null;
        v14 = cluster;
        cluster = null;
        v15 = v;
        v = null;
        result__8982__auto__10631 = ((IFn)new cluster$reset_ref$fn__10625(v12, v13, v14, v15)).invoke();
        elapsed_10623 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__10630);
        msec_10624 = ((IFn)cluster$reset_ref.const__16.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_10623));
        v16 = (IFn)cluster$reset_ref.const__17.getRawRoot();
        v17 = m_10622;
        m_10622 = null;
        v18 = msec_10624;
        msec_10624 = null;
        v19 = ((IFn)cluster$reset_ref.const__12.getRawRoot()).invoke((Object)v17, (Object)cluster$reset_ref.const__18, v18, (Object)cluster$reset_ref.const__13, (Object)cluster$reset_ref.const__19);
        v20 = cluster$reset_ref.__thunk__1__;
        v21 = result__8982__auto__10631;
        v22 = v20.get(v21);
        if (v20 == v22) {
            cluster$reset_ref.__thunk__1__ = cluster$reset_ref.__site__1__.fault(v21);
            v22 = cluster$reset_ref.__thunk__1__.get(v21);
        }
        if (v22 != null && v22 != Boolean.FALSE) {
            v23 = new Object[2];
            v23[0] = cluster$reset_ref.const__20;
            v24 = (IFn)cluster$reset_ref.const__21.getRawRoot();
            v25 = cluster$reset_ref.__thunk__2__;
            v26 = result__8982__auto__10631;
            v27 = v25.get(v26);
            if (v25 == v27) {
                cluster$reset_ref.__thunk__2__ = cluster$reset_ref.__site__2__.fault(v26);
                v27 = cluster$reset_ref.__thunk__2__.get(v26);
            }
            v23[1] = v24.invoke(v27);
            v28 = RT.mapUniqueKeys((Object[])v23);
        } else {
            v28 = null;
        }
        endmsg__8984__auto__10628 = v16.invoke(v19, v28);
        logger = LoggerFactory.getLogger((String)"datomic.cluster");
        if (logger.isInfoEnabled()) {
            v29 = logger;
            logger = null;
            v30 = endmsg__8984__auto__10628;
            endmsg__8984__auto__10628 = null;
            v29.info((String)((IFn)cluster$reset_ref.const__11.getRawRoot()).invoke(v30));
        }
        v31 = ((IFn)cluster$reset_ref.const__22.getRawRoot()).invoke(result__8982__auto__10631, (Object)cluster$reset_ref.const__23);
        if (v31 != null && v31 != Boolean.FALSE) {
            v32 = cluster$reset_ref.__thunk__3__;
            v33 = result__8982__auto__10631;
            result__8982__auto__10631 = null;
            v34 = v32.get(v33);
            if (v32 == v34) {
                cluster$reset_ref.__thunk__3__ = cluster$reset_ref.__site__3__.fault(v33);
                v34 = cluster$reset_ref.__thunk__3__.get(v33);
            }
        } else {
            v35 = cluster$reset_ref.__thunk__4__;
            v36 = result__8982__auto__10631;
            result__8982__auto__10631 = null;
            v37 = v35.get(v36);
            if (v35 == v37) {
                cluster$reset_ref.__thunk__4__ = cluster$reset_ref.__site__4__.fault(v36);
                v37 = cluster$reset_ref.__thunk__4__.get(v36);
            }
            throw (Throwable)v37;
        }
        return v34;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cluster$reset_ref.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__2 = RT.var((String)"clojure.core", (String)"seq?");
        const__3 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.keyword(null, (String)"rev");
        const__6 = RT.keyword(null, (String)"event");
        const__7 = RT.keyword((String)"kv-cluster", (String)"reset-ref");
        const__8 = RT.keyword(null, (String)"key");
        const__9 = RT.keyword(null, (String)"from");
        const__10 = RT.keyword(null, (String)"to");
        const__11 = RT.var((String)"datomic.slf4j", (String)"process");
        const__12 = RT.var((String)"clojure.core", (String)"assoc");
        const__13 = RT.keyword(null, (String)"phase");
        const__14 = RT.keyword(null, (String)"begin");
        const__16 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
        const__17 = RT.var((String)"clojure.core", (String)"merge");
        const__18 = RT.keyword(null, (String)"msec");
        const__19 = RT.keyword(null, (String)"end");
        const__20 = RT.keyword(null, (String)"threw");
        const__21 = RT.var((String)"clojure.core", (String)"class");
        const__22 = RT.var((String)"clojure.core", (String)"contains?");
        const__23 = RT.keyword(null, (String)"returned");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__2__ = __site__2__;
        __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
        __thunk__3__ = __site__3__;
        __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__4__ = __site__4__;
    }
}

