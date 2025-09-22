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
import datomic.integrity$crosscheck_dir_segs$fn__22282;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class integrity$crosscheck_dir_segs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"cluster");
    public static final Keyword const__4 = RT.keyword(null, (String)"olookup");
    public static final AFn const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"integrity", (String)"crosscheck-dir-segs")});
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
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object p__22277, Object progress) {
        Object object;
        IPersistentMap iPersistentMap;
        Object object2;
        Object map__22278 = p__22277;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__22278);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__22278;
            map__22278 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object2 = map__22278;
            map__22278 = null;
        }
        Object map__222782 = object2;
        Object cluster2 = RT.get((Object)map__222782, (Object)const__3);
        Object olookup = RT.get((Object)map__222782, (Object)const__4);
        AFn m_22279 = const__7;
        Logger logger = LoggerFactory.getLogger((String)"datomic.integrity");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)m_22279, (Object)const__10, (Object)const__11)));
        }
        Object ___8980__auto__22304 = null;
        long start__8981__auto__22305 = System.nanoTime();
        Object object5 = p__22277;
        p__22277 = null;
        Object object6 = olookup;
        olookup = null;
        Object object7 = map__222782;
        map__222782 = null;
        Object object8 = cluster2;
        cluster2 = null;
        Object v8 = ___8980__auto__22304;
        ___8980__auto__22304 = null;
        Object object9 = progress;
        progress = null;
        Object result__8982__auto__22306 = ((IFn)new integrity$crosscheck_dir_segs$fn__22282(object5, object6, object7, start__8981__auto__22305, m_22279, object8, v8, object9)).invoke();
        long elapsed_22280 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__22305);
        Object msec_22281 = ((IFn)const__13.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_22280));
        IFn iFn = (IFn)const__14.getRawRoot();
        AFn aFn = m_22279;
        m_22279 = null;
        Object object10 = msec_22281;
        msec_22281 = null;
        Object object11 = ((IFn)const__9.getRawRoot()).invoke((Object)aFn, (Object)const__15, object10, (Object)const__10, (Object)const__16);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object12 = result__8982__auto__22306;
        Object object13 = iLookupThunk.get(object12);
        if (iLookupThunk == object13) {
            __thunk__0__ = __site__0__.fault(object12);
            object13 = __thunk__0__.get(object12);
        }
        if (object13 != null && object13 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__17;
            IFn iFn2 = (IFn)const__18.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object14 = result__8982__auto__22306;
            Object object15 = iLookupThunk2.get(object14);
            if (iLookupThunk2 == object15) {
                __thunk__1__ = __site__1__.fault(object14);
                object15 = __thunk__1__.get(object14);
            }
            objectArray[1] = iFn2.invoke(object15);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__22303 = iFn.invoke(object11, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.integrity");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object16 = endmsg__8984__auto__22303;
            endmsg__8984__auto__22303 = null;
            logger4.debug((String)((IFn)const__8.getRawRoot()).invoke(object16));
        }
        Object object17 = ((IFn)const__19.getRawRoot()).invoke(result__8982__auto__22306, (Object)const__20);
        if (object17 != null && object17 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object18 = result__8982__auto__22306;
            result__8982__auto__22306 = null;
            object = iLookupThunk3.get(object18);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object18);
                object = __thunk__2__.get(object18);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object19 = result__8982__auto__22306;
            result__8982__auto__22306 = null;
            Object object20 = iLookupThunk4.get(object19);
            if (iLookupThunk4 == object20) {
                __thunk__3__ = __site__3__.fault(object19);
                object20 = __thunk__3__.get(object19);
            }
            throw (Throwable)object20;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$crosscheck_dir_segs.invokeStatic(object3, object4);
    }
}

