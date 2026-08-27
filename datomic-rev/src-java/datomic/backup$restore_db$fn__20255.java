/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.backup$restore_db$fn__20255$fn__20259;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class backup$restore_db$fn__20255
extends AFunction {
    Object t;
    Object db_id;
    Object log_root_node;
    Object job;
    Object index_top_node;
    Object to_cluster;
    Object restore;
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"restore", (String)"db");
    public static final Keyword const__2 = RT.keyword(null, (String)"t");
    public static final Keyword const__3 = RT.keyword(null, (String)"db-id");
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__6 = RT.keyword(null, (String)"phase");
    public static final Keyword const__7 = RT.keyword(null, (String)"begin");
    public static final Var const__9 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__11 = RT.keyword(null, (String)"msec");
    public static final Keyword const__12 = RT.keyword(null, (String)"end");
    public static final Keyword const__13 = RT.keyword(null, (String)"threw");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__16 = RT.keyword(null, (String)"returned");
    public static final Var const__17 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    public static final Var const__18 = RT.var((String)"datomic.monitor", (String)"alarm");
    public static final Keyword const__19 = RT.keyword(null, (String)"UnhandledException");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public backup$restore_db$fn__20255(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.t = object;
        this.db_id = object2;
        this.log_root_node = object3;
        this.job = object4;
        this.index_top_node = object5;
        this.to_cluster = object6;
        this.restore = object7;
    }

    public Object invoke() {
        Object object;
        try {
            Object object2;
            IPersistentMap iPersistentMap;
            this.t = null;
            this.db_id = null;
            IPersistentMap m_20256 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, this.t, const__3, this.db_id});
            Logger logger = LoggerFactory.getLogger((String)"datomic.backup");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.info((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)m_20256, (Object)const__6, (Object)const__7)));
            }
            long start__8981__auto__20264 = System.nanoTime();
            this.log_root_node = null;
            this.job = null;
            this.index_top_node = null;
            this.to_cluster = null;
            this.restore = null;
            Object result__8982__auto__20265 = ((IFn)new backup$restore_db$fn__20255$fn__20259(this.log_root_node, this.job, this.index_top_node, this.to_cluster, this.restore)).invoke();
            long elapsed_20257 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__20264);
            Object msec_20258 = ((IFn)const__9.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_20257));
            IFn iFn = (IFn)const__10.getRawRoot();
            IPersistentMap iPersistentMap2 = m_20256;
            m_20256 = null;
            Object object3 = msec_20258;
            msec_20258 = null;
            Object object4 = ((IFn)const__5.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__11, object3, (Object)const__6, (Object)const__12);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object5 = result__8982__auto__20265;
            Object object6 = iLookupThunk.get(object5);
            if (iLookupThunk == object6) {
                __thunk__0__ = __site__0__.fault(object5);
                object6 = __thunk__0__.get(object5);
            }
            if (object6 != null && object6 != Boolean.FALSE) {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__13;
                IFn iFn2 = (IFn)const__14.getRawRoot();
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object7 = result__8982__auto__20265;
                Object object8 = iLookupThunk2.get(object7);
                if (iLookupThunk2 == object8) {
                    __thunk__1__ = __site__1__.fault(object7);
                    object8 = __thunk__1__.get(object7);
                }
                objectArray[1] = iFn2.invoke(object8);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                iPersistentMap = null;
            }
            Object endmsg__8984__auto__20262 = iFn.invoke(object4, iPersistentMap);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.backup");
            if (logger3.isInfoEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                Object object9 = endmsg__8984__auto__20262;
                endmsg__8984__auto__20262 = null;
                logger4.info((String)((IFn)const__4.getRawRoot()).invoke(object9));
            }
            Object object10 = ((IFn)const__15.getRawRoot()).invoke(result__8982__auto__20265, (Object)const__16);
            if (object10 != null && object10 != Boolean.FALSE) {
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object11 = result__8982__auto__20265;
                result__8982__auto__20265 = null;
                object2 = iLookupThunk3.get(object11);
                if (iLookupThunk3 == object2) {
                    __thunk__2__ = __site__2__.fault(object11);
                    object2 = __thunk__2__.get(object11);
                }
            } else {
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object12 = result__8982__auto__20265;
                result__8982__auto__20265 = null;
                Object object13 = iLookupThunk4.get(object12);
                if (iLookupThunk4 == object13) {
                    __thunk__3__ = __site__3__.fault(object12);
                    object13 = __thunk__3__.get(object12);
                }
                throw (Throwable)object13;
            }
            object = object2;
        }
        catch (Throwable t__9147__auto__2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.backup");
            Throwable ex = t__9147__auto__2;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)const__4.getRawRoot()).invoke((Object)"error executing future"), ex);
                Logger logger5 = logger;
                logger = null;
                Throwable throwable = ex;
                ex = null;
                ((IFn)const__17.getRawRoot()).invoke((Object)logger5, (Object)throwable);
            }
            ((IFn)const__18.getRawRoot()).invoke((Object)const__19);
            Object t__9147__auto__2 = null;
            throw t__9147__auto__2;
        }
        return object;
    }
}

