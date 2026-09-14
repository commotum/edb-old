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
 *  clojure.lang.Tuple
 *  clojure.lang.Util
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
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.ddb_values$get_value$fn__20424;
import datomic.ddb_values$get_value$fn__20429;
import datomic.ddb_values$get_value$fn__20431;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ddb_values$get_value
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"ddb-values", (String)"get-value");
    public static final Keyword const__2 = RT.keyword(null, (String)"id");
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
    public static final Var const__16 = RT.var((String)"datomic.ddb-values", (String)"split-map-keys-with");
    public static final Object const__19 = 1L;
    public static final Keyword const__21 = RT.keyword(null, (String)"v");
    public static final Var const__23 = RT.var((String)"datomic.io", (String)"base128->bbuf");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"pmap");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"__n"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"v"));
    static ILookupThunk __thunk__5__ = __site__5__;

    public static Object invokeStatic(Object ddb_client, Object table, Object id) {
        Object object;
        Object temp__5457__auto__20450;
        Object object2;
        IPersistentMap iPersistentMap;
        IPersistentMap m_20421 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, id});
        Logger logger = LoggerFactory.getLogger((String)"datomic.ddb-values");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)m_20421, (Object)const__5, (Object)const__6)));
        }
        long start__8981__auto__20447 = System.nanoTime();
        Object result__8982__auto__20448 = ((IFn)new ddb_values$get_value$fn__20424(ddb_client, table, id)).invoke();
        long elapsed_20422 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__20447);
        Object msec_20423 = ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_20422));
        IFn iFn = (IFn)const__9.getRawRoot();
        IPersistentMap iPersistentMap2 = m_20421;
        m_20421 = null;
        Object object3 = msec_20423;
        msec_20423 = null;
        Object object4 = ((IFn)const__4.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__10, object3, (Object)const__5, (Object)const__11);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object5 = result__8982__auto__20448;
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
            Object object7 = result__8982__auto__20448;
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
        Object endmsg__8984__auto__20445 = iFn.invoke(object4, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.ddb-values");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object9 = endmsg__8984__auto__20445;
            endmsg__8984__auto__20445 = null;
            logger4.debug((String)((IFn)const__3.getRawRoot()).invoke(object9));
        }
        Object object10 = ((IFn)const__14.getRawRoot()).invoke(result__8982__auto__20448, (Object)const__15);
        if (object10 != null && object10 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object11 = result__8982__auto__20448;
            result__8982__auto__20448 = null;
            object2 = iLookupThunk3.get(object11);
            if (iLookupThunk3 == object2) {
                __thunk__2__ = __site__2__.fault(object11);
                object2 = __thunk__2__.get(object11);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object12 = result__8982__auto__20448;
            result__8982__auto__20448 = null;
            Object object13 = iLookupThunk4.get(object12);
            if (iLookupThunk4 == object13) {
                __thunk__3__ = __site__3__.fault(object12);
                object13 = __thunk__3__.get(object12);
            }
            throw (Throwable)object13;
        }
        Object object14 = temp__5457__auto__20450 = object2;
        if (object14 != null && object14 != Boolean.FALSE) {
            Object v;
            Object result2;
            Object object15 = temp__5457__auto__20450;
            temp__5457__auto__20450 = null;
            Object object16 = result2 = object15;
            result2 = null;
            Object vec__20426 = ((IFn)const__16.getRawRoot()).invoke(object16, (Object)new ddb_values$get_value$fn__20429());
            Object impl2 = RT.nth((Object)vec__20426, (int)RT.intCast((long)0L), null);
            Object object17 = vec__20426;
            vec__20426 = null;
            Object user = RT.nth((Object)object17, (int)RT.intCast((long)1L), null);
            ILookupThunk iLookupThunk5 = __thunk__4__;
            Object object18 = impl2;
            impl2 = null;
            Object object19 = iLookupThunk5.get(object18);
            if (iLookupThunk5 == object19) {
                __thunk__4__ = __site__4__.fault(object18);
                object19 = __thunk__4__.get(object18);
            }
            Object n = object19;
            ILookupThunk iLookupThunk6 = __thunk__5__;
            Object object20 = user;
            Object object21 = iLookupThunk6.get(object20);
            if (iLookupThunk6 == object21) {
                __thunk__5__ = __site__5__.fault(object20);
                object21 = v = __thunk__5__.get(object20);
            }
            if (Util.equiv((Object)n, (long)1L)) {
                Object and__5236__auto__20449;
                Object object22 = and__5236__auto__20449 = v;
                if (object22 != null && object22 != Boolean.FALSE) {
                    Object object23 = user;
                    user = null;
                    Object object24 = v;
                    v = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object23, (Object)const__21, ((IFn)const__23.getRawRoot()).invoke(object24));
                } else {
                    object = and__5236__auto__20449;
                    and__5236__auto__20449 = null;
                }
            } else {
                Object object25 = v;
                v = null;
                Object object26 = ddb_client;
                ddb_client = null;
                Object object27 = table;
                table = null;
                Object object28 = id;
                id = null;
                Object object29 = n;
                n = null;
                Object chunks = ((IFn)const__24.getRawRoot()).invoke((Object)Tuple.create((Object)object25), ((IFn)const__25.getRawRoot()).invoke((Object)new ddb_values$get_value$fn__20431(object26, object27, object28), ((IFn)const__26.getRawRoot()).invoke(const__19, object29)));
                Object object30 = ((IFn)const__27.getRawRoot()).invoke(const__28.getRawRoot(), chunks);
                if (object30 != null && object30 != Boolean.FALSE) {
                    Object object31 = user;
                    user = null;
                    Object object32 = chunks;
                    chunks = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object31, (Object)const__21, ((IFn)const__23.getRawRoot()).invoke(((IFn)const__29.getRawRoot()).invoke(const__30.getRawRoot(), object32)));
                } else {
                    object = null;
                }
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return ddb_values$get_value.invokeStatic(object4, object5, object6);
    }
}

