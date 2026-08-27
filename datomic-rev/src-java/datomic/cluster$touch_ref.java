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
import datomic.cluster$touch_ref$fn__10636;
import datomic.cluster.ClusteredStore;
import org.slf4j.LoggerFactory;

public final class cluster$touch_ref
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
    public static final Var const__9;
    public static final Var const__10;
    public static final Keyword const__11;
    public static final Keyword const__12;
    public static final Var const__14;
    public static final Var const__15;
    public static final Keyword const__16;
    public static final Keyword const__17;
    public static final Keyword const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final Keyword const__21;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cluster, Object k) {
        v0 = (IFn)cluster$touch_ref.const__0.getRawRoot();
        v1 = cluster;
        if (Util.classOf((Object)v1) == cluster$touch_ref.__cached_class__0) ** GOTO lbl7
        if (!(v1 instanceof ClusteredStore)) {
            v1 = v1;
            cluster$touch_ref.__cached_class__0 = Util.classOf((Object)v1);
lbl7:
            // 2 sources

            v2 = cluster$touch_ref.const__1.getRawRoot().invoke(v1, k);
        } else {
            v2 = ((ClusteredStore)v1).get_ref(k);
        }
        map__10632 = v0.invoke(v2);
        v3 = ((IFn)cluster$touch_ref.const__2.getRawRoot()).invoke(map__10632);
        if (v3 != null && v3 != Boolean.FALSE) {
            v4 = map__10632;
            map__10632 = null;
            v5 = PersistentHashMap.create((ISeq)((ISeq)((IFn)cluster$touch_ref.const__3.getRawRoot()).invoke(v4)));
        } else {
            v5 = map__10632;
            map__10632 = null;
        }
        prev = map__10632 = v5;
        v6 = map__10632;
        map__10632 = null;
        rev = RT.get((Object)v6, (Object)cluster$touch_ref.const__5);
        m_10633 = RT.mapUniqueKeys((Object[])new Object[]{cluster$touch_ref.const__6, cluster$touch_ref.const__7, cluster$touch_ref.const__8, prev});
        logger = LoggerFactory.getLogger((String)"datomic.cluster");
        if (logger.isWarnEnabled()) {
            v7 = logger;
            logger = null;
            v7.warn((String)((IFn)cluster$touch_ref.const__9.getRawRoot()).invoke(((IFn)cluster$touch_ref.const__10.getRawRoot()).invoke((Object)m_10633, (Object)cluster$touch_ref.const__11, (Object)cluster$touch_ref.const__12)));
        }
        start__8981__auto__10641 = System.nanoTime();
        v8 = rev;
        rev = null;
        v9 = prev;
        prev = null;
        v10 = k;
        k = null;
        v11 = cluster;
        cluster = null;
        result__8982__auto__10642 = ((IFn)new cluster$touch_ref$fn__10636(v8, v9, v10, v11)).invoke();
        elapsed_10634 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__10641);
        msec_10635 = ((IFn)cluster$touch_ref.const__14.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_10634));
        v12 = (IFn)cluster$touch_ref.const__15.getRawRoot();
        v13 = m_10633;
        m_10633 = null;
        v14 = msec_10635;
        msec_10635 = null;
        v15 = ((IFn)cluster$touch_ref.const__10.getRawRoot()).invoke((Object)v13, (Object)cluster$touch_ref.const__16, v14, (Object)cluster$touch_ref.const__11, (Object)cluster$touch_ref.const__17);
        v16 = cluster$touch_ref.__thunk__0__;
        v17 = result__8982__auto__10642;
        v18 = v16.get(v17);
        if (v16 == v18) {
            cluster$touch_ref.__thunk__0__ = cluster$touch_ref.__site__0__.fault(v17);
            v18 = cluster$touch_ref.__thunk__0__.get(v17);
        }
        if (v18 != null && v18 != Boolean.FALSE) {
            v19 = new Object[2];
            v19[0] = cluster$touch_ref.const__18;
            v20 = (IFn)cluster$touch_ref.const__19.getRawRoot();
            v21 = cluster$touch_ref.__thunk__1__;
            v22 = result__8982__auto__10642;
            v23 = v21.get(v22);
            if (v21 == v23) {
                cluster$touch_ref.__thunk__1__ = cluster$touch_ref.__site__1__.fault(v22);
                v23 = cluster$touch_ref.__thunk__1__.get(v22);
            }
            v19[1] = v20.invoke(v23);
            v24 = RT.mapUniqueKeys((Object[])v19);
        } else {
            v24 = null;
        }
        endmsg__8984__auto__10639 = v12.invoke(v15, v24);
        logger = LoggerFactory.getLogger((String)"datomic.cluster");
        if (logger.isWarnEnabled()) {
            v25 = logger;
            logger = null;
            v26 = endmsg__8984__auto__10639;
            endmsg__8984__auto__10639 = null;
            v25.warn((String)((IFn)cluster$touch_ref.const__9.getRawRoot()).invoke(v26));
        }
        v27 = ((IFn)cluster$touch_ref.const__20.getRawRoot()).invoke(result__8982__auto__10642, (Object)cluster$touch_ref.const__21);
        if (v27 != null && v27 != Boolean.FALSE) {
            v28 = cluster$touch_ref.__thunk__2__;
            v29 = result__8982__auto__10642;
            result__8982__auto__10642 = null;
            v30 = v28.get(v29);
            if (v28 == v30) {
                cluster$touch_ref.__thunk__2__ = cluster$touch_ref.__site__2__.fault(v29);
                v30 = cluster$touch_ref.__thunk__2__.get(v29);
            }
        } else {
            v31 = cluster$touch_ref.__thunk__3__;
            v32 = result__8982__auto__10642;
            result__8982__auto__10642 = null;
            v33 = v31.get(v32);
            if (v31 == v33) {
                cluster$touch_ref.__thunk__3__ = cluster$touch_ref.__site__3__.fault(v32);
                v33 = cluster$touch_ref.__thunk__3__.get(v32);
            }
            throw (Throwable)v33;
        }
        return v30;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cluster$touch_ref.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__2 = RT.var((String)"clojure.core", (String)"seq?");
        const__3 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.keyword(null, (String)"rev");
        const__6 = RT.keyword(null, (String)"event");
        const__7 = RT.keyword((String)"kv-cluster", (String)"touch-ref");
        const__8 = RT.keyword(null, (String)"from");
        const__9 = RT.var((String)"datomic.slf4j", (String)"process");
        const__10 = RT.var((String)"clojure.core", (String)"assoc");
        const__11 = RT.keyword(null, (String)"phase");
        const__12 = RT.keyword(null, (String)"begin");
        const__14 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
        const__15 = RT.var((String)"clojure.core", (String)"merge");
        const__16 = RT.keyword(null, (String)"msec");
        const__17 = RT.keyword(null, (String)"end");
        const__18 = RT.keyword(null, (String)"threw");
        const__19 = RT.var((String)"clojure.core", (String)"class");
        const__20 = RT.var((String)"clojure.core", (String)"contains?");
        const__21 = RT.keyword(null, (String)"returned");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
        __thunk__2__ = __site__2__;
        __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__3__ = __site__3__;
    }
}

