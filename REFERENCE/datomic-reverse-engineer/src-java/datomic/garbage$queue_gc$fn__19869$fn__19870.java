/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.Dbid;
import datomic.garbage$queue_gc$fn__19869$fn__19870$fn__19874;
import org.slf4j.LoggerFactory;

public final class garbage$queue_gc$fn__19869$fn__19870
extends AFunction {
    Object cluster;
    Object older_than;
    private static Class __cached_class__0;
    public static final Keyword const__0;
    public static final Keyword const__1;
    public static final Keyword const__2;
    public static final Var const__3;
    public static final Keyword const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Var const__10;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Keyword const__17;
    public static final Var const__18;
    public static final Keyword const__19;
    public static final Var const__20;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;

    public garbage$queue_gc$fn__19869$fn__19870(Object object, Object object2) {
        this.cluster = object;
        this.older_than = object2;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = new Object[6];
            v0[0] = garbage$queue_gc$fn__19869$fn__19870.const__0;
            v0[1] = garbage$queue_gc$fn__19869$fn__19870.const__1;
            v0[2] = garbage$queue_gc$fn__19869$fn__19870.const__2;
            v1 = this.cluster;
            if (Util.classOf((Object)v1) == garbage$queue_gc$fn__19869$fn__19870.__cached_class__0) ** GOTO lbl11
            if (!(v1 instanceof Dbid)) {
                v1 = v1;
                garbage$queue_gc$fn__19869$fn__19870.__cached_class__0 = Util.classOf((Object)v1);
lbl11:
                // 2 sources

                v2 = garbage$queue_gc$fn__19869$fn__19870.const__3.getRawRoot().invoke(v1);
            } else {
                v2 = ((Dbid)v1).dbId();
            }
            v0[3] = v2;
            v0[4] = garbage$queue_gc$fn__19869$fn__19870.const__4;
            v0[5] = this.older_than;
            m_19871 = RT.mapUniqueKeys((Object[])v0);
            logger = LoggerFactory.getLogger((String)"datomic.garbage");
            if (logger.isInfoEnabled()) {
                v3 = logger;
                logger = null;
                v3.info((String)((IFn)garbage$queue_gc$fn__19869$fn__19870.const__5.getRawRoot()).invoke(((IFn)garbage$queue_gc$fn__19869$fn__19870.const__6.getRawRoot()).invoke((Object)m_19871, (Object)garbage$queue_gc$fn__19869$fn__19870.const__7, (Object)garbage$queue_gc$fn__19869$fn__19870.const__8)));
            }
            start__8981__auto__19879 = System.nanoTime();
            this.cluster = null;
            this.older_than = null;
            result__8982__auto__19880 = ((IFn)new garbage$queue_gc$fn__19869$fn__19870$fn__19874(this.cluster, this.older_than)).invoke();
            elapsed_19872 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__19879);
            msec_19873 = ((IFn)garbage$queue_gc$fn__19869$fn__19870.const__10.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_19872));
            v4 = (IFn)garbage$queue_gc$fn__19869$fn__19870.const__11.getRawRoot();
            v5 = m_19871;
            m_19871 = null;
            v6 = msec_19873;
            msec_19873 = null;
            v7 = ((IFn)garbage$queue_gc$fn__19869$fn__19870.const__6.getRawRoot()).invoke((Object)v5, (Object)garbage$queue_gc$fn__19869$fn__19870.const__12, v6, (Object)garbage$queue_gc$fn__19869$fn__19870.const__7, (Object)garbage$queue_gc$fn__19869$fn__19870.const__13);
            v8 = garbage$queue_gc$fn__19869$fn__19870.__thunk__0__;
            v9 = result__8982__auto__19880;
            v10 = v8.get(v9);
            if (v8 == v10) {
                garbage$queue_gc$fn__19869$fn__19870.__thunk__0__ = garbage$queue_gc$fn__19869$fn__19870.__site__0__.fault(v9);
                v10 = garbage$queue_gc$fn__19869$fn__19870.__thunk__0__.get(v9);
            }
            if (v10 != null && v10 != Boolean.FALSE) {
                v11 = new Object[2];
                v11[0] = garbage$queue_gc$fn__19869$fn__19870.const__14;
                v12 = (IFn)garbage$queue_gc$fn__19869$fn__19870.const__15.getRawRoot();
                v13 = garbage$queue_gc$fn__19869$fn__19870.__thunk__1__;
                v14 = result__8982__auto__19880;
                v15 = v13.get(v14);
                if (v13 == v15) {
                    garbage$queue_gc$fn__19869$fn__19870.__thunk__1__ = garbage$queue_gc$fn__19869$fn__19870.__site__1__.fault(v14);
                    v15 = garbage$queue_gc$fn__19869$fn__19870.__thunk__1__.get(v14);
                }
                v11[1] = v12.invoke(v15);
                v16 = RT.mapUniqueKeys((Object[])v11);
            } else {
                v16 = null;
            }
            endmsg__8984__auto__19877 = v4.invoke(v7, v16);
            logger = LoggerFactory.getLogger((String)"datomic.garbage");
            if (logger.isInfoEnabled()) {
                v17 = logger;
                logger = null;
                v18 = endmsg__8984__auto__19877;
                endmsg__8984__auto__19877 = null;
                v17.info((String)((IFn)garbage$queue_gc$fn__19869$fn__19870.const__5.getRawRoot()).invoke(v18));
            }
            v19 = ((IFn)garbage$queue_gc$fn__19869$fn__19870.const__16.getRawRoot()).invoke(result__8982__auto__19880, (Object)garbage$queue_gc$fn__19869$fn__19870.const__17);
            if (v19 != null && v19 != Boolean.FALSE) {
                v20 = garbage$queue_gc$fn__19869$fn__19870.__thunk__2__;
                v21 = result__8982__auto__19880;
                result__8982__auto__19880 = null;
                v22 = v20.get(v21);
                if (v20 == v22) {
                    garbage$queue_gc$fn__19869$fn__19870.__thunk__2__ = garbage$queue_gc$fn__19869$fn__19870.__site__2__.fault(v21);
                    v22 = garbage$queue_gc$fn__19869$fn__19870.__thunk__2__.get(v21);
                }
            } else {
                v23 = garbage$queue_gc$fn__19869$fn__19870.__thunk__3__;
                v24 = result__8982__auto__19880;
                result__8982__auto__19880 = null;
                v25 = v23.get(v24);
                if (v23 == v25) {
                    garbage$queue_gc$fn__19869$fn__19870.__thunk__3__ = garbage$queue_gc$fn__19869$fn__19870.__site__3__.fault(v24);
                    v25 = garbage$queue_gc$fn__19869$fn__19870.__thunk__3__.get(v24);
                }
                throw (Throwable)v25;
            }
            var11_12 = v22;
        }
        catch (Throwable t) {
            ((IFn)garbage$queue_gc$fn__19869$fn__19870.const__18.getRawRoot()).invoke((Object)garbage$queue_gc$fn__19869$fn__19870.const__19);
            logger = LoggerFactory.getLogger((String)"datomic.garbage");
            t = null;
            ex = t;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)garbage$queue_gc$fn__19869$fn__19870.const__5.getRawRoot()).invoke((Object)"Cluster gc failed"), ex);
                v26 = logger;
                logger = null;
                v27 = ex;
                ex = null;
                ((IFn)garbage$queue_gc$fn__19869$fn__19870.const__20.getRawRoot()).invoke((Object)v26, (Object)v27);
            }
            var11_12 = null;
        }
        return var11_12;
    }

    static {
        const__0 = RT.keyword(null, (String)"event");
        const__1 = RT.keyword((String)"garbage", (String)"collect");
        const__2 = RT.keyword(null, (String)"dbid");
        const__3 = RT.var((String)"datomic.cluster", (String)"dbId");
        const__4 = RT.keyword(null, (String)"older-than");
        const__5 = RT.var((String)"datomic.slf4j", (String)"process");
        const__6 = RT.var((String)"clojure.core", (String)"assoc");
        const__7 = RT.keyword(null, (String)"phase");
        const__8 = RT.keyword(null, (String)"begin");
        const__10 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
        const__11 = RT.var((String)"clojure.core", (String)"merge");
        const__12 = RT.keyword(null, (String)"msec");
        const__13 = RT.keyword(null, (String)"end");
        const__14 = RT.keyword(null, (String)"threw");
        const__15 = RT.var((String)"clojure.core", (String)"class");
        const__16 = RT.var((String)"clojure.core", (String)"contains?");
        const__17 = RT.keyword(null, (String)"returned");
        const__18 = RT.var((String)"datomic.monitor", (String)"alarm");
        const__19 = RT.keyword(null, (String)"StorageGCFailed");
        const__20 = RT.var((String)"datomic.slf4j", (String)"caused-by");
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

