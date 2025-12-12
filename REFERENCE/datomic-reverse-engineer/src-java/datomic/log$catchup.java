/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
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
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Database;
import datomic.log$catchup$fn__16366;
import datomic.log$catchup$fn__16371;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class log$catchup
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.log", (String)"catchup");
    public static final Var const__1 = RT.var((String)"datomic.log", (String)"seek-tx");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"future-call");
    public static final Var const__3 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__5 = RT.var((String)"datomic.log", (String)"catchup-tx");
    public static final Keyword const__6 = RT.keyword(null, (String)"db");
    public static final Keyword const__7 = RT.keyword(null, (String)"size");
    public static final Object const__8 = 0L;
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__13 = RT.keyword(null, (String)"event");
    public static final AFn const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"log", (String)"catchup-fulltext")});
    public static final Var const__16 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__18 = RT.keyword(null, (String)"phase");
    public static final Keyword const__19 = RT.keyword(null, (String)"begin");
    public static final Var const__20 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__22 = RT.keyword(null, (String)"msec");
    public static final Keyword const__23 = RT.keyword(null, (String)"end");
    public static final Keyword const__24 = RT.keyword(null, (String)"threw");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__27 = RT.keyword(null, (String)"returned");
    public static final Keyword const__28 = RT.keyword((String)"log", (String)"catchup");
    public static final Keyword const__29 = RT.keyword(null, (String)"bytes");
    public static final Keyword const__30 = RT.keyword(null, (String)"tail-t");
    public static final Keyword const__31 = RT.keyword(null, (String)"index-t");
    public static final Var const__32 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__33 = RT.keyword(null, (String)"LogIngestMsec");
    public static final Keyword const__34 = RT.keyword(null, (String)"LogIngestBytes");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object db2, Object log2, Object catchup_ft) {
        Object object;
        Logger logger;
        long elapsed;
        Object size;
        long index_t;
        block14: {
            Object db3;
            block13: {
                IPersistentMap iPersistentMap;
                Object object2;
                Object iter2;
                long start = System.currentTimeMillis();
                index_t = ((Database)db2).basisT();
                Object object3 = log2;
                log2 = null;
                Object object4 = iter2 = ((IFn)const__1.getRawRoot()).invoke(object3, (Object)Numbers.num((long)((Database)db2).nextT()));
                Object object5 = object4 != null && object4 != Boolean.FALSE ? ((IFn)const__2.getRawRoot()).invoke((Object)new log$catchup$fn__16366(iter2)) : null;
                Object object6 = iter2;
                iter2 = null;
                Object log_txes = ((IFn)const__3.getRawRoot()).invoke(object6);
                Object[] objectArray = new Object[4];
                objectArray[0] = const__6;
                Object object7 = db2;
                db2 = null;
                objectArray[1] = object7;
                objectArray[2] = const__7;
                objectArray[3] = const__8;
                Object map__16365 = ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])objectArray), log_txes);
                Object object8 = ((IFn)const__9.getRawRoot()).invoke(map__16365);
                if (object8 != null && object8 != Boolean.FALSE) {
                    Object object9 = map__16365;
                    map__16365 = null;
                    object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__10.getRawRoot()).invoke(object9)));
                } else {
                    object2 = map__16365;
                    map__16365 = null;
                }
                Object map__163652 = object2;
                db3 = RT.get((Object)map__163652, (Object)const__6);
                Object object10 = map__163652;
                map__163652 = null;
                size = RT.get((Object)object10, (Object)const__7);
                elapsed = Numbers.minus((long)System.currentTimeMillis(), (long)start);
                Object object11 = catchup_ft;
                catchup_ft = null;
                if (object11 == null || object11 == Boolean.FALSE) break block13;
                AFn m_16368 = const__15;
                logger = LoggerFactory.getLogger((String)"datomic.log");
                if (logger.isInfoEnabled()) {
                    Logger logger2 = logger;
                    logger = null;
                    logger2.info((String)((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)m_16368, (Object)const__18, (Object)const__19)));
                }
                long start__8981__auto__16376 = System.nanoTime();
                Object object12 = log_txes;
                log_txes = null;
                Object object13 = db3;
                db3 = null;
                Object result__8982__auto__16377 = ((IFn)new log$catchup$fn__16371(object12, object13)).invoke();
                long elapsed_16369 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__16376);
                Object msec_16370 = ((IFn)const__20.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_16369));
                IFn iFn = (IFn)const__21.getRawRoot();
                AFn aFn = m_16368;
                m_16368 = null;
                Object object14 = msec_16370;
                msec_16370 = null;
                Object object15 = ((IFn)const__17.getRawRoot()).invoke((Object)aFn, (Object)const__22, object14, (Object)const__18, (Object)const__23);
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object16 = result__8982__auto__16377;
                Object object17 = iLookupThunk.get(object16);
                if (iLookupThunk == object17) {
                    __thunk__0__ = __site__0__.fault(object16);
                    object17 = __thunk__0__.get(object16);
                }
                if (object17 != null && object17 != Boolean.FALSE) {
                    Object[] objectArray2 = new Object[2];
                    objectArray2[0] = const__24;
                    IFn iFn2 = (IFn)const__25.getRawRoot();
                    ILookupThunk iLookupThunk2 = __thunk__1__;
                    Object object18 = result__8982__auto__16377;
                    Object object19 = iLookupThunk2.get(object18);
                    if (iLookupThunk2 == object19) {
                        __thunk__1__ = __site__1__.fault(object18);
                        object19 = __thunk__1__.get(object18);
                    }
                    objectArray2[1] = iFn2.invoke(object19);
                    iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
                } else {
                    iPersistentMap = null;
                }
                Object endmsg__8984__auto__16374 = iFn.invoke(object15, iPersistentMap);
                Logger logger3 = LoggerFactory.getLogger((String)"datomic.log");
                if (logger3.isInfoEnabled()) {
                    Logger logger4 = logger3;
                    logger3 = null;
                    Object object20 = endmsg__8984__auto__16374;
                    endmsg__8984__auto__16374 = null;
                    logger4.info((String)((IFn)const__16.getRawRoot()).invoke(object20));
                }
                Object object21 = ((IFn)const__26.getRawRoot()).invoke(result__8982__auto__16377, (Object)const__27);
                if (object21 != null && object21 != Boolean.FALSE) {
                    ILookupThunk iLookupThunk3 = __thunk__2__;
                    Object object22 = result__8982__auto__16377;
                    result__8982__auto__16377 = null;
                    object = iLookupThunk3.get(object22);
                    if (iLookupThunk3 == object) {
                        __thunk__2__ = __site__2__.fault(object22);
                        object = __thunk__2__.get(object22);
                    }
                    break block14;
                } else {
                    ILookupThunk iLookupThunk4 = __thunk__3__;
                    Object object23 = result__8982__auto__16377;
                    result__8982__auto__16377 = null;
                    Object object24 = iLookupThunk4.get(object23);
                    if (iLookupThunk4 != object24) {
                        throw (Throwable)object24;
                    }
                    __thunk__3__ = __site__3__.fault(object23);
                    object24 = __thunk__3__.get(object23);
                    throw (Throwable)object24;
                }
            }
            object = db3;
            db3 = null;
        }
        Object db4 = object;
        logger = LoggerFactory.getLogger((String)"datomic.log");
        if (logger.isInfoEnabled()) {
            Logger logger5 = logger;
            logger = null;
            logger5.info((String)((IFn)const__16.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__13, const__28, const__29, size, const__30, Numbers.num((long)((Database)db4).basisT()), const__31, Numbers.num((long)index_t), const__22, Numbers.num((long)elapsed)})));
        }
        ((IFn)const__32.getRawRoot()).invoke((Object)const__33, (Object)Numbers.num((long)elapsed));
        ((IFn)const__32.getRawRoot()).invoke((Object)const__34, size);
        Object[] objectArray = new Object[4];
        objectArray[0] = const__6;
        Object object25 = db4;
        db4 = null;
        objectArray[1] = object25;
        objectArray[2] = const__7;
        Object object26 = size;
        size = null;
        objectArray[3] = object26;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return log$catchup.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object db2, Object log2) {
        Object object = db2;
        db2 = null;
        Object object2 = log2;
        log2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)Boolean.FALSE);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$catchup.invokeStatic(object3, object4);
    }
}

