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
import datomic.fulltext$do_indexing_job$fn__14614;
import datomic.fulltext$do_indexing_job$fn__14616;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class fulltext$do_indexing_job
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.fulltext", (String)"create-indexing-job");
    public static final Keyword const__1 = RT.keyword(null, (String)"event");
    public static final Keyword const__2 = RT.keyword((String)"index", (String)"build-fulltext-local");
    public static final Keyword const__3 = RT.keyword(null, (String)"attr");
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
    public static final Var const__17 = RT.var((String)"datomic.fulltext", (String)"promote-to-cluster");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"future-call");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object cstore, Object olookup, Object add_data, Object remove_data, Object attr_id, Object dirid) {
        Object object;
        Object object2 = cstore;
        cstore = null;
        Object object3 = olookup;
        olookup = null;
        Object object4 = dirid;
        dirid = null;
        Object job = ((IFn)const__0.getRawRoot()).invoke(object2, object3, object4);
        try {
            IPersistentMap iPersistentMap;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__1;
            objectArray[1] = const__2;
            objectArray[2] = const__3;
            Object object5 = attr_id;
            attr_id = null;
            objectArray[3] = object5;
            IPersistentMap m_14611 = RT.mapUniqueKeys((Object[])objectArray);
            Logger logger = LoggerFactory.getLogger((String)"datomic.fulltext");
            if (logger.isDebugEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.debug((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)m_14611, (Object)const__6, (Object)const__7)));
            }
            long start__8981__auto__14630 = System.nanoTime();
            Object object6 = remove_data;
            remove_data = null;
            Object object7 = add_data;
            add_data = null;
            Object result__8982__auto__14631 = ((IFn)new fulltext$do_indexing_job$fn__14614(job, object6, object7)).invoke();
            long elapsed_14612 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__14630);
            Object msec_14613 = ((IFn)const__9.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_14612));
            IFn iFn = (IFn)const__10.getRawRoot();
            IPersistentMap iPersistentMap2 = m_14611;
            m_14611 = null;
            Object object8 = msec_14613;
            msec_14613 = null;
            Object object9 = ((IFn)const__5.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__11, object8, (Object)const__6, (Object)const__12);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object10 = result__8982__auto__14631;
            Object object11 = iLookupThunk.get(object10);
            if (iLookupThunk == object11) {
                __thunk__0__ = __site__0__.fault(object10);
                object11 = __thunk__0__.get(object10);
            }
            if (object11 != null && object11 != Boolean.FALSE) {
                Object[] objectArray2 = new Object[2];
                objectArray2[0] = const__13;
                IFn iFn2 = (IFn)const__14.getRawRoot();
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object12 = result__8982__auto__14631;
                Object object13 = iLookupThunk2.get(object12);
                if (iLookupThunk2 == object13) {
                    __thunk__1__ = __site__1__.fault(object12);
                    object13 = __thunk__1__.get(object12);
                }
                objectArray2[1] = iFn2.invoke(object13);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
            } else {
                iPersistentMap = null;
            }
            Object endmsg__8984__auto__14628 = iFn.invoke(object9, iPersistentMap);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.fulltext");
            if (logger3.isDebugEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                Object object14 = endmsg__8984__auto__14628;
                endmsg__8984__auto__14628 = null;
                logger4.debug((String)((IFn)const__4.getRawRoot()).invoke(object14));
            }
            Object object15 = ((IFn)const__15.getRawRoot()).invoke(result__8982__auto__14631, (Object)const__16);
            if (object15 != null && object15 != Boolean.FALSE) {
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object16 = result__8982__auto__14631;
                result__8982__auto__14631 = null;
                Object object17 = iLookupThunk3.get(object16);
                if (iLookupThunk3 == object17) {
                    __thunk__2__ = __site__2__.fault(object16);
                    object17 = __thunk__2__.get(object16);
                }
            } else {
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object18 = result__8982__auto__14631;
                result__8982__auto__14631 = null;
                Object object19 = iLookupThunk4.get(object18);
                if (iLookupThunk4 == object19) {
                    __thunk__3__ = __site__3__.fault(object18);
                    object19 = __thunk__3__.get(object18);
                }
                throw (Throwable)object19;
            }
            object = ((IFn)const__17.getRawRoot()).invoke(job);
        }
        finally {
            Object object20 = job;
            job = null;
            ((IFn)const__18.getRawRoot()).invoke((Object)new fulltext$do_indexing_job$fn__14616(object20));
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return fulltext$do_indexing_job.invokeStatic(object7, object8, object9, object10, object11, object12);
    }
}

