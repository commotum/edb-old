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
import datomic.db$add_fulltext$fn__14059;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class db$add_fulltext
extends AFunction {
    public static final AFn const__2 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"db", (String)"add-fulltext")});
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__5 = RT.keyword(null, (String)"phase");
    public static final Keyword const__6 = RT.keyword(null, (String)"begin");
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__9 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__10 = RT.keyword(null, (String)"DbAddFulltextMsec");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__12 = RT.keyword(null, (String)"msec");
    public static final Keyword const__13 = RT.keyword(null, (String)"end");
    public static final Keyword const__14 = RT.keyword(null, (String)"threw");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__17 = RT.keyword(null, (String)"returned");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object db2, Object data2) {
        Object object;
        IPersistentMap iPersistentMap;
        AFn m_14056 = const__2;
        Logger logger = LoggerFactory.getLogger((String)"datomic.db");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)m_14056, (Object)const__5, (Object)const__6)));
        }
        long start__8981__auto__14068 = System.nanoTime();
        Object object2 = db2;
        db2 = null;
        Object object3 = data2;
        data2 = null;
        Object result__8982__auto__14069 = ((IFn)new db$add_fulltext$fn__14059(object2, object3)).invoke();
        long elapsed_14057 = System.nanoTime() - start__8981__auto__14068;
        Object msec_14058 = ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_14057));
        ((IFn)const__9.getRawRoot()).invoke((Object)const__10, msec_14058);
        IFn iFn = (IFn)const__11.getRawRoot();
        AFn aFn = m_14056;
        m_14056 = null;
        Object object4 = msec_14058;
        msec_14058 = null;
        Object object5 = ((IFn)const__4.getRawRoot()).invoke((Object)aFn, (Object)const__12, object4, (Object)const__5, (Object)const__13);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object6 = result__8982__auto__14069;
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        if (object7 != null && object7 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__14;
            IFn iFn2 = (IFn)const__15.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object8 = result__8982__auto__14069;
            Object object9 = iLookupThunk2.get(object8);
            if (iLookupThunk2 == object9) {
                __thunk__1__ = __site__1__.fault(object8);
                object9 = __thunk__1__.get(object8);
            }
            objectArray[1] = iFn2.invoke(object9);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__14066 = iFn.invoke(object5, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.db");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object10 = endmsg__8984__auto__14066;
            endmsg__8984__auto__14066 = null;
            logger4.debug((String)((IFn)const__3.getRawRoot()).invoke(object10));
        }
        Object object11 = ((IFn)const__16.getRawRoot()).invoke(result__8982__auto__14069, (Object)const__17);
        if (object11 != null && object11 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object12 = result__8982__auto__14069;
            result__8982__auto__14069 = null;
            object = iLookupThunk3.get(object12);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object12);
                object = __thunk__2__.get(object12);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object13 = result__8982__auto__14069;
            result__8982__auto__14069 = null;
            Object object14 = iLookupThunk4.get(object13);
            if (iLookupThunk4 == object14) {
                __thunk__3__ = __site__3__.fault(object13);
                object14 = __thunk__3__.get(object13);
            }
            throw (Throwable)object14;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$add_fulltext.invokeStatic(object3, object4);
    }
}

