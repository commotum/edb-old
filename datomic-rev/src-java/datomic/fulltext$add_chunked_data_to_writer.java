/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.fulltext$add_chunked_data_to_writer$fn__14567;
import datomic.fulltext$add_chunked_data_to_writer$fn__14572;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class fulltext$add_chunked_data_to_writer
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"partition-all");
    public static final Object const__2 = 1000L;
    public static final AFn const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"index", (String)"fulltext-datoms")});
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__10 = RT.keyword(null, (String)"phase");
    public static final Keyword const__11 = RT.keyword(null, (String)"begin");
    public static final Var const__13 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__15 = RT.keyword(null, (String)"msec");
    public static final Keyword const__16 = RT.keyword(null, (String)"end");
    public static final Keyword const__17 = RT.keyword(null, (String)"threw");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__20 = RT.keyword(null, (String)"returned");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"first");
    public static final AFn const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"index", (String)"fulltext-datoms")});
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"next");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__7__ = __site__7__;

    public static Object invokeStatic(Object writer2, Object data2) {
        Object object = data2;
        data2 = null;
        Object seq_14560 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2, object));
        Object chunk_14561 = null;
        long count_14562 = 0L;
        long i_14563 = 0L;
        while (true) {
            IPersistentMap iPersistentMap;
            Object temp__5457__auto__14584;
            if (i_14563 < count_14562) {
                IPersistentMap iPersistentMap2;
                Object datums = ((Indexed)chunk_14561).nth(RT.intCast((long)i_14563));
                AFn m_14564 = const__7;
                Logger logger = LoggerFactory.getLogger((String)"datomic.fulltext");
                if (logger.isInfoEnabled()) {
                    Logger logger2 = logger;
                    logger = null;
                    logger2.info((String)((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)m_14564, (Object)const__10, (Object)const__11)));
                }
                long start__8981__auto__14577 = System.nanoTime();
                Object object2 = datums;
                datums = null;
                Object result__8982__auto__14578 = ((IFn)new fulltext$add_chunked_data_to_writer$fn__14567(writer2, object2)).invoke();
                long elapsed_14565 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__14577);
                Object msec_14566 = ((IFn)const__13.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_14565));
                IFn iFn = (IFn)const__14.getRawRoot();
                AFn aFn = m_14564;
                m_14564 = null;
                Object object3 = msec_14566;
                msec_14566 = null;
                Object object4 = ((IFn)const__9.getRawRoot()).invoke((Object)aFn, (Object)const__15, object3, (Object)const__10, (Object)const__16);
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object5 = result__8982__auto__14578;
                Object object6 = iLookupThunk.get(object5);
                if (iLookupThunk == object6) {
                    __thunk__0__ = __site__0__.fault(object5);
                    object6 = __thunk__0__.get(object5);
                }
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object[] objectArray = new Object[2];
                    objectArray[0] = const__17;
                    IFn iFn2 = (IFn)const__18.getRawRoot();
                    ILookupThunk iLookupThunk2 = __thunk__1__;
                    Object object7 = result__8982__auto__14578;
                    Object object8 = iLookupThunk2.get(object7);
                    if (iLookupThunk2 == object8) {
                        __thunk__1__ = __site__1__.fault(object7);
                        object8 = __thunk__1__.get(object7);
                    }
                    objectArray[1] = iFn2.invoke(object8);
                    iPersistentMap2 = RT.mapUniqueKeys((Object[])objectArray);
                } else {
                    iPersistentMap2 = null;
                }
                Object endmsg__8984__auto__14575 = iFn.invoke(object4, iPersistentMap2);
                Logger logger3 = LoggerFactory.getLogger((String)"datomic.fulltext");
                if (logger3.isInfoEnabled()) {
                    Logger logger4 = logger3;
                    logger3 = null;
                    Object object9 = endmsg__8984__auto__14575;
                    endmsg__8984__auto__14575 = null;
                    logger4.info((String)((IFn)const__8.getRawRoot()).invoke(object9));
                }
                Object object10 = ((IFn)const__19.getRawRoot()).invoke(result__8982__auto__14578, (Object)const__20);
                if (object10 != null && object10 != Boolean.FALSE) {
                    ILookupThunk iLookupThunk3 = __thunk__2__;
                    Object object11 = result__8982__auto__14578;
                    result__8982__auto__14578 = null;
                    Object object12 = iLookupThunk3.get(object11);
                    if (iLookupThunk3 == object12) {
                        __thunk__2__ = __site__2__.fault(object11);
                        object12 = __thunk__2__.get(object11);
                    }
                } else {
                    ILookupThunk iLookupThunk4 = __thunk__3__;
                    Object object13 = result__8982__auto__14578;
                    result__8982__auto__14578 = null;
                    Object object14 = iLookupThunk4.get(object13);
                    if (iLookupThunk4 == object14) {
                        __thunk__3__ = __site__3__.fault(object13);
                        object14 = __thunk__3__.get(object13);
                    }
                    throw (Throwable)object14;
                }
                Object object15 = seq_14560;
                seq_14560 = null;
                Object object16 = chunk_14561;
                chunk_14561 = null;
                ++i_14563;
                chunk_14561 = object16;
                seq_14560 = object15;
                continue;
            }
            Object object17 = seq_14560;
            seq_14560 = null;
            Object object18 = temp__5457__auto__14584 = ((IFn)const__0.getRawRoot()).invoke(object17);
            if (object18 == null || object18 == Boolean.FALSE) break;
            Object object19 = temp__5457__auto__14584;
            temp__5457__auto__14584 = null;
            Object seq_145602 = object19;
            Object object20 = ((IFn)const__22.getRawRoot()).invoke(seq_145602);
            if (object20 != null && object20 != Boolean.FALSE) {
                Object c__5719__auto__14579 = ((IFn)const__23.getRawRoot()).invoke(seq_145602);
                Object object21 = seq_145602;
                seq_145602 = null;
                Object object22 = c__5719__auto__14579;
                Object object23 = c__5719__auto__14579;
                c__5719__auto__14579 = null;
                i_14563 = RT.intCast((long)0L);
                count_14562 = RT.intCast((int)RT.count((Object)object23));
                chunk_14561 = object22;
                seq_14560 = ((IFn)const__24.getRawRoot()).invoke(object21);
                continue;
            }
            Object datums = ((IFn)const__27.getRawRoot()).invoke(seq_145602);
            AFn m_14569 = const__28;
            Logger logger = LoggerFactory.getLogger((String)"datomic.fulltext");
            if (logger.isInfoEnabled()) {
                Logger logger5 = logger;
                logger = null;
                logger5.info((String)((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)m_14569, (Object)const__10, (Object)const__11)));
            }
            long start__8981__auto__14582 = System.nanoTime();
            Object object24 = datums;
            datums = null;
            Object result__8982__auto__14583 = ((IFn)new fulltext$add_chunked_data_to_writer$fn__14572(writer2, object24)).invoke();
            long elapsed_14570 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__14582);
            Object msec_14571 = ((IFn)const__13.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_14570));
            IFn iFn = (IFn)const__14.getRawRoot();
            AFn aFn = m_14569;
            m_14569 = null;
            Object object25 = msec_14571;
            msec_14571 = null;
            Object object26 = ((IFn)const__9.getRawRoot()).invoke((Object)aFn, (Object)const__15, object25, (Object)const__10, (Object)const__16);
            ILookupThunk iLookupThunk = __thunk__4__;
            Object object27 = result__8982__auto__14583;
            Object object28 = iLookupThunk.get(object27);
            if (iLookupThunk == object28) {
                __thunk__4__ = __site__4__.fault(object27);
                object28 = __thunk__4__.get(object27);
            }
            if (object28 != null && object28 != Boolean.FALSE) {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__17;
                IFn iFn3 = (IFn)const__18.getRawRoot();
                ILookupThunk iLookupThunk5 = __thunk__5__;
                Object object29 = result__8982__auto__14583;
                Object object30 = iLookupThunk5.get(object29);
                if (iLookupThunk5 == object30) {
                    __thunk__5__ = __site__5__.fault(object29);
                    object30 = __thunk__5__.get(object29);
                }
                objectArray[1] = iFn3.invoke(object30);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                iPersistentMap = null;
            }
            Object endmsg__8984__auto__14580 = iFn.invoke(object26, iPersistentMap);
            Logger logger6 = LoggerFactory.getLogger((String)"datomic.fulltext");
            if (logger6.isInfoEnabled()) {
                Logger logger7 = logger6;
                logger6 = null;
                Object object31 = endmsg__8984__auto__14580;
                endmsg__8984__auto__14580 = null;
                logger7.info((String)((IFn)const__8.getRawRoot()).invoke(object31));
            }
            Object object32 = ((IFn)const__19.getRawRoot()).invoke(result__8982__auto__14583, (Object)const__20);
            if (object32 != null && object32 != Boolean.FALSE) {
                ILookupThunk iLookupThunk6 = __thunk__6__;
                Object object33 = result__8982__auto__14583;
                result__8982__auto__14583 = null;
                Object object34 = iLookupThunk6.get(object33);
                if (iLookupThunk6 == object34) {
                    __thunk__6__ = __site__6__.fault(object33);
                    object34 = __thunk__6__.get(object33);
                }
            } else {
                ILookupThunk iLookupThunk7 = __thunk__7__;
                Object object35 = result__8982__auto__14583;
                result__8982__auto__14583 = null;
                Object object36 = iLookupThunk7.get(object35);
                if (iLookupThunk7 == object36) {
                    __thunk__7__ = __site__7__.fault(object35);
                    object36 = __thunk__7__.get(object35);
                }
                throw (Throwable)object36;
            }
            Object object37 = seq_145602;
            seq_145602 = null;
            i_14563 = 0L;
            count_14562 = 0L;
            chunk_14561 = null;
            seq_14560 = ((IFn)const__29.getRawRoot()).invoke(object37);
        }
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fulltext$add_chunked_data_to_writer.invokeStatic(object3, object4);
    }
}

