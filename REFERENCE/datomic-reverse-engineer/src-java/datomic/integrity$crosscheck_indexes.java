/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
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

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.integrity$crosscheck_indexes$fn__22207;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class integrity$crosscheck_indexes
extends AFunction {
    public static final AFn const__2 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"integrity", (String)"crosscheck-indexes")});
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__5 = RT.keyword(null, (String)"phase");
    public static final Keyword const__6 = RT.keyword(null, (String)"begin");
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__10 = RT.keyword(null, (String)"msec");
    public static final Keyword const__11 = RT.keyword(null, (String)"end");
    public static final Keyword const__12 = RT.keyword(null, (String)"threw");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__15 = RT.keyword(null, (String)"returned");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object o) {
        Object object;
        IPersistentMap iPersistentMap;
        AFn m_22204 = const__2;
        Logger logger = LoggerFactory.getLogger((String)"datomic.integrity");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)m_22204, (Object)const__5, (Object)const__6)));
        }
        long start__8981__auto__22224 = System.nanoTime();
        Object object2 = o;
        o = null;
        Object result__8982__auto__22225 = ((IFn)new integrity$crosscheck_indexes$fn__22207(object2)).invoke();
        long elapsed_22205 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__22224);
        Object msec_22206 = ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_22205));
        IFn iFn = (IFn)const__9.getRawRoot();
        AFn aFn = m_22204;
        m_22204 = null;
        Object object3 = msec_22206;
        msec_22206 = null;
        Object object4 = ((IFn)const__4.getRawRoot()).invoke((Object)aFn, (Object)const__10, object3, (Object)const__5, (Object)const__11);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object5 = result__8982__auto__22225;
        Object object6 = iLookupThunk.get(object5);
        if (iLookupThunk == object6) {
            __thunk__0__ = __site__0__.fault(object5);
            object6 = __thunk__0__.get(object5);
        }
        if (object6 != null && object6 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__12;
            IFn iFn2 = (IFn)const__13.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object7 = result__8982__auto__22225;
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
        Object endmsg__8984__auto__22222 = iFn.invoke(object4, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.integrity");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object9 = endmsg__8984__auto__22222;
            endmsg__8984__auto__22222 = null;
            logger4.debug((String)((IFn)const__3.getRawRoot()).invoke(object9));
        }
        Object object10 = ((IFn)const__14.getRawRoot()).invoke(result__8982__auto__22225, (Object)const__15);
        if (object10 != null && object10 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object11 = result__8982__auto__22225;
            result__8982__auto__22225 = null;
            object = iLookupThunk3.get(object11);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object11);
                object = __thunk__2__.get(object11);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object12 = result__8982__auto__22225;
            result__8982__auto__22225 = null;
            Object object13 = iLookupThunk4.get(object12);
            if (iLookupThunk4 == object13) {
                __thunk__3__ = __site__3__.fault(object12);
                object13 = __thunk__3__.get(object12);
            }
            throw (Throwable)object13;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$crosscheck_indexes.invokeStatic(object2);
    }
}

