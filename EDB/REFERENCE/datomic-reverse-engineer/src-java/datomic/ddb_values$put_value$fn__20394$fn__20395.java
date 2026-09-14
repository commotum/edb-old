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
import datomic.ddb_values$put_value$fn__20394$fn__20395$fn__20399;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ddb_values$put_value$fn__20394$fn__20395
extends AFunction {
    Object n;
    Object chunks;
    Object id;
    Object table;
    Object ddb_client;
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"ddb-values", (String)"put-value-chunk");
    public static final Keyword const__2 = RT.keyword(null, (String)"id");
    public static final Keyword const__3 = RT.keyword(null, (String)"n");
    public static final Keyword const__4 = RT.keyword(null, (String)"bufsize");
    public static final Var const__7 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__9 = RT.keyword(null, (String)"phase");
    public static final Keyword const__10 = RT.keyword(null, (String)"begin");
    public static final Var const__12 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__13 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__14 = RT.keyword(null, (String)"DdbPutChunkMsec");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__16 = RT.keyword(null, (String)"msec");
    public static final Keyword const__17 = RT.keyword(null, (String)"end");
    public static final Keyword const__18 = RT.keyword(null, (String)"threw");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__21 = RT.keyword(null, (String)"returned");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public ddb_values$put_value$fn__20394$fn__20395(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.n = object;
        this.chunks = object2;
        this.id = object3;
        this.table = object4;
        this.ddb_client = object5;
    }

    public Object invoke() {
        Object object;
        IPersistentMap iPersistentMap;
        IPersistentMap m_20396 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, this.id, const__3, this.n, const__4, RT.count((Object)RT.nth((Object)this.chunks, (int)RT.intCast((Object)((Number)this.n))))});
        Logger logger = LoggerFactory.getLogger((String)"datomic.ddb-values");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)m_20396, (Object)const__9, (Object)const__10)));
        }
        long start__8981__auto__20406 = System.nanoTime();
        this.n = null;
        this.chunks = null;
        this.id = null;
        this.table = null;
        this.ddb_client = null;
        Object result__8982__auto__20407 = ((IFn)new ddb_values$put_value$fn__20394$fn__20395$fn__20399(this.n, this.chunks, this.id, this.table, this.ddb_client)).invoke();
        long elapsed_20397 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__20406);
        Object msec_20398 = ((IFn)const__12.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_20397));
        ((IFn)const__13.getRawRoot()).invoke((Object)const__14, msec_20398);
        IFn iFn = (IFn)const__15.getRawRoot();
        IPersistentMap iPersistentMap2 = m_20396;
        m_20396 = null;
        Object object2 = msec_20398;
        msec_20398 = null;
        Object object3 = ((IFn)const__8.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__16, object2, (Object)const__9, (Object)const__17);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object4 = result__8982__auto__20407;
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__0__ = __site__0__.fault(object4);
            object5 = __thunk__0__.get(object4);
        }
        if (object5 != null && object5 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__18;
            IFn iFn2 = (IFn)const__19.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object6 = result__8982__auto__20407;
            Object object7 = iLookupThunk2.get(object6);
            if (iLookupThunk2 == object7) {
                __thunk__1__ = __site__1__.fault(object6);
                object7 = __thunk__1__.get(object6);
            }
            objectArray[1] = iFn2.invoke(object7);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__20404 = iFn.invoke(object3, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.ddb-values");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object8 = endmsg__8984__auto__20404;
            endmsg__8984__auto__20404 = null;
            logger4.debug((String)((IFn)const__7.getRawRoot()).invoke(object8));
        }
        Object object9 = ((IFn)const__20.getRawRoot()).invoke(result__8982__auto__20407, (Object)const__21);
        if (object9 != null && object9 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object10 = result__8982__auto__20407;
            result__8982__auto__20407 = null;
            object = iLookupThunk3.get(object10);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object10);
                object = __thunk__2__.get(object10);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object11 = result__8982__auto__20407;
            result__8982__auto__20407 = null;
            Object object12 = iLookupThunk4.get(object11);
            if (iLookupThunk4 == object12) {
                __thunk__3__ = __site__3__.fault(object11);
                object12 = __thunk__3__.get(object11);
            }
            throw (Throwable)object12;
        }
        return object;
    }
}

