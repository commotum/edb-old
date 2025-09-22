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
import datomic.fulltext$do_indexing_job$fn__14616$fn__14620;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class fulltext$do_indexing_job$fn__14616
extends AFunction {
    Object job;
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"index", (String)"cleanup-fulltext");
    public static final Keyword const__2 = RT.keyword(null, (String)"dir");
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
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"path"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public fulltext$do_indexing_job$fn__14616(Object object) {
        this.job = object;
    }

    public Object invoke() {
        Object object;
        try {
            Object object2;
            IPersistentMap iPersistentMap;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__0;
            objectArray[1] = const__1;
            objectArray[2] = const__2;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = this.job;
            Object object4 = iLookupThunk.get(object3);
            if (iLookupThunk == object4) {
                __thunk__0__ = __site__0__.fault(object3);
                object4 = __thunk__0__.get(object3);
            }
            objectArray[3] = object4;
            IPersistentMap m_14617 = RT.mapUniqueKeys((Object[])objectArray);
            Logger logger = LoggerFactory.getLogger((String)"datomic.fulltext");
            if (logger.isDebugEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.debug((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)m_14617, (Object)const__6, (Object)const__7)));
            }
            long start__8981__auto__14625 = System.nanoTime();
            this.job = null;
            Object result__8982__auto__14626 = ((IFn)new fulltext$do_indexing_job$fn__14616$fn__14620(this.job)).invoke();
            long elapsed_14618 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__14625);
            Object msec_14619 = ((IFn)const__9.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_14618));
            IFn iFn = (IFn)const__10.getRawRoot();
            IPersistentMap iPersistentMap2 = m_14617;
            m_14617 = null;
            Object object5 = msec_14619;
            msec_14619 = null;
            Object object6 = ((IFn)const__5.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__11, object5, (Object)const__6, (Object)const__12);
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object7 = result__8982__auto__14626;
            Object object8 = iLookupThunk2.get(object7);
            if (iLookupThunk2 == object8) {
                __thunk__1__ = __site__1__.fault(object7);
                object8 = __thunk__1__.get(object7);
            }
            if (object8 != null && object8 != Boolean.FALSE) {
                Object[] objectArray2 = new Object[2];
                objectArray2[0] = const__13;
                IFn iFn2 = (IFn)const__14.getRawRoot();
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object9 = result__8982__auto__14626;
                Object object10 = iLookupThunk3.get(object9);
                if (iLookupThunk3 == object10) {
                    __thunk__2__ = __site__2__.fault(object9);
                    object10 = __thunk__2__.get(object9);
                }
                objectArray2[1] = iFn2.invoke(object10);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
            } else {
                iPersistentMap = null;
            }
            Object endmsg__8984__auto__14623 = iFn.invoke(object6, iPersistentMap);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.fulltext");
            if (logger3.isDebugEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                Object object11 = endmsg__8984__auto__14623;
                endmsg__8984__auto__14623 = null;
                logger4.debug((String)((IFn)const__4.getRawRoot()).invoke(object11));
            }
            Object object12 = ((IFn)const__15.getRawRoot()).invoke(result__8982__auto__14626, (Object)const__16);
            if (object12 != null && object12 != Boolean.FALSE) {
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object13 = result__8982__auto__14626;
                result__8982__auto__14626 = null;
                object2 = iLookupThunk4.get(object13);
                if (iLookupThunk4 == object2) {
                    __thunk__3__ = __site__3__.fault(object13);
                    object2 = __thunk__3__.get(object13);
                }
            } else {
                ILookupThunk iLookupThunk5 = __thunk__4__;
                Object object14 = result__8982__auto__14626;
                result__8982__auto__14626 = null;
                Object object15 = iLookupThunk5.get(object14);
                if (iLookupThunk5 == object15) {
                    __thunk__4__ = __site__4__.fault(object14);
                    object15 = __thunk__4__.get(object14);
                }
                throw (Throwable)object15;
            }
            object = object2;
        }
        catch (Throwable t__9147__auto__2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.fulltext");
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

