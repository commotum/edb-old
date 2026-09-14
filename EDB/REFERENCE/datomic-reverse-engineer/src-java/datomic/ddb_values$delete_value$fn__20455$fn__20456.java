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
import datomic.ddb_values$delete_value$fn__20455$fn__20456$fn__20460;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ddb_values$delete_value$fn__20455$fn__20456
extends AFunction {
    Object ddb_client;
    Object id;
    Object table;
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"ddb-values", (String)"delete-value-chunk");
    public static final Keyword const__2 = RT.keyword(null, (String)"id");
    public static final Keyword const__3 = RT.keyword(null, (String)"n");
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
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public ddb_values$delete_value$fn__20455$fn__20456(Object object, Object object2, Object object3) {
        this.ddb_client = object;
        this.id = object2;
        this.table = object3;
    }

    public Object invoke(Object p1__20451_SHARP_) {
        Object object;
        IPersistentMap iPersistentMap;
        IPersistentMap m_20457 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, this.id, const__3, p1__20451_SHARP_});
        Logger logger = LoggerFactory.getLogger((String)"datomic.ddb-values");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)m_20457, (Object)const__6, (Object)const__7)));
        }
        long start__8981__auto__20465 = System.nanoTime();
        Object object2 = p1__20451_SHARP_;
        p1__20451_SHARP_ = null;
        Object result__8982__auto__20466 = ((IFn)new ddb_values$delete_value$fn__20455$fn__20456$fn__20460(this.ddb_client, this.id, object2, this.table)).invoke();
        long elapsed_20458 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__20465);
        Object msec_20459 = ((IFn)const__9.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_20458));
        IFn iFn = (IFn)const__10.getRawRoot();
        IPersistentMap iPersistentMap2 = m_20457;
        m_20457 = null;
        Object object3 = msec_20459;
        msec_20459 = null;
        Object object4 = ((IFn)const__5.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__11, object3, (Object)const__6, (Object)const__12);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object5 = result__8982__auto__20466;
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
            Object object7 = result__8982__auto__20466;
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
        Object endmsg__8984__auto__20463 = iFn.invoke(object4, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.ddb-values");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object9 = endmsg__8984__auto__20463;
            endmsg__8984__auto__20463 = null;
            logger4.debug((String)((IFn)const__4.getRawRoot()).invoke(object9));
        }
        Object object10 = ((IFn)const__15.getRawRoot()).invoke(result__8982__auto__20466, (Object)const__16);
        if (object10 != null && object10 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object11 = result__8982__auto__20466;
            result__8982__auto__20466 = null;
            object = iLookupThunk3.get(object11);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object11);
                object = __thunk__2__.get(object11);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object12 = result__8982__auto__20466;
            result__8982__auto__20466 = null;
            Object object13 = iLookupThunk4.get(object12);
            if (iLookupThunk4 == object13) {
                __thunk__3__ = __site__3__.fault(object12);
                object13 = __thunk__3__.get(object12);
            }
            throw (Throwable)object13;
        }
        return object;
    }
}

