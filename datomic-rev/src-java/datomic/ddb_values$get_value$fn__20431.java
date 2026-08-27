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
import datomic.ddb_values$get_value$fn__20431$fn__20435;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ddb_values$get_value$fn__20431
extends AFunction {
    Object ddb_client;
    Object table;
    Object id;
    public static final Keyword const__1 = RT.keyword(null, (String)"event");
    public static final Keyword const__2 = RT.keyword((String)"ddb-values", (String)"get-value-chunk");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
    public static final Keyword const__4 = RT.keyword(null, (String)"n");
    public static final Var const__5 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__7 = RT.keyword(null, (String)"phase");
    public static final Keyword const__8 = RT.keyword(null, (String)"begin");
    public static final Var const__10 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
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
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"v"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public ddb_values$get_value$fn__20431(Object object, Object object2, Object object3) {
        this.ddb_client = object;
        this.table = object2;
        this.id = object3;
    }

    public Object invoke(Object p1__20420_SHARP_) {
        Object object;
        IPersistentMap iPersistentMap;
        ILookupThunk iLookupThunk = __thunk__4__;
        IPersistentMap m_20432 = RT.mapUniqueKeys((Object[])new Object[]{const__1, const__2, const__3, this.id, const__4, p1__20420_SHARP_});
        Logger logger = LoggerFactory.getLogger((String)"datomic.ddb-values");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)m_20432, (Object)const__7, (Object)const__8)));
        }
        long start__8981__auto__20442 = System.nanoTime();
        Object object2 = p1__20420_SHARP_;
        p1__20420_SHARP_ = null;
        Object result__8982__auto__20443 = ((IFn)new ddb_values$get_value$fn__20431$fn__20435(this.ddb_client, this.table, object2, this.id)).invoke();
        long elapsed_20433 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__20442);
        Object msec_20434 = ((IFn)const__10.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_20433));
        IFn iFn = (IFn)const__11.getRawRoot();
        IPersistentMap iPersistentMap2 = m_20432;
        m_20432 = null;
        Object object3 = msec_20434;
        msec_20434 = null;
        Object object4 = ((IFn)const__6.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__12, object3, (Object)const__7, (Object)const__13);
        ILookupThunk iLookupThunk2 = __thunk__0__;
        Object object5 = result__8982__auto__20443;
        Object object6 = iLookupThunk2.get(object5);
        if (iLookupThunk2 == object6) {
            __thunk__0__ = __site__0__.fault(object5);
            object6 = __thunk__0__.get(object5);
        }
        if (object6 != null && object6 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__14;
            IFn iFn2 = (IFn)const__15.getRawRoot();
            ILookupThunk iLookupThunk3 = __thunk__1__;
            Object object7 = result__8982__auto__20443;
            Object object8 = iLookupThunk3.get(object7);
            if (iLookupThunk3 == object8) {
                __thunk__1__ = __site__1__.fault(object7);
                object8 = __thunk__1__.get(object7);
            }
            objectArray[1] = iFn2.invoke(object8);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__20440 = iFn.invoke(object4, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.ddb-values");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object9 = endmsg__8984__auto__20440;
            endmsg__8984__auto__20440 = null;
            logger4.debug((String)((IFn)const__5.getRawRoot()).invoke(object9));
        }
        Object object10 = ((IFn)const__16.getRawRoot()).invoke(result__8982__auto__20443, (Object)const__17);
        if (object10 != null && object10 != Boolean.FALSE) {
            ILookupThunk iLookupThunk4 = __thunk__2__;
            Object object11 = result__8982__auto__20443;
            result__8982__auto__20443 = null;
            object = iLookupThunk4.get(object11);
            if (iLookupThunk4 == object) {
                __thunk__2__ = __site__2__.fault(object11);
                object = __thunk__2__.get(object11);
            }
        } else {
            ILookupThunk iLookupThunk5 = __thunk__3__;
            Object object12 = result__8982__auto__20443;
            result__8982__auto__20443 = null;
            Object object13 = iLookupThunk5.get(object12);
            if (iLookupThunk5 == object13) {
                __thunk__3__ = __site__3__.fault(object12);
                object13 = __thunk__3__.get(object12);
            }
            throw (Throwable)object13;
        }
        Object object14 = iLookupThunk.get(object);
        if (iLookupThunk == object14) {
            __thunk__4__ = __site__4__.fault(object);
            object14 = __thunk__4__.get(object);
        }
        return object14;
    }
}

