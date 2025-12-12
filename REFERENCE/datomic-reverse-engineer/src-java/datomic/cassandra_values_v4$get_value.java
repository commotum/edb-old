/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cassandra_values_v4$get_value$fn__10316;
import datomic.cassandra_values_v4$get_value$fn__10319;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class cassandra_values_v4$get_value
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"cassandra-values", (String)"get-value");
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
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__19 = RT.keyword(null, (String)"chunks");
    public static final Keyword const__20 = RT.keyword(null, (String)"val");
    public static final Keyword const__21 = RT.keyword(null, (String)"map");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"read-string");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Object const__25 = 1L;
    public static final Keyword const__26 = RT.keyword(null, (String)"v");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"pmap");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__32 = RT.var((String)"datomic.io", (String)"unchunk");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"map"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"map"));
    static ILookupThunk __thunk__5__ = __site__5__;

    public static Object invokeStatic(Object session, Object table, Object id) {
        Object object;
        Object temp__5457__auto__10339;
        Object object2;
        IPersistentMap iPersistentMap;
        IPersistentMap m_10313 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, id});
        Logger logger = LoggerFactory.getLogger((String)"datomic.cassandra-values-v4");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)m_10313, (Object)const__5, (Object)const__6)));
        }
        long start__8981__auto__10335 = System.nanoTime();
        Object result__8982__auto__10336 = ((IFn)new cassandra_values_v4$get_value$fn__10316(id, session, table)).invoke();
        long elapsed_10314 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__10335);
        Object msec_10315 = ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_10314));
        IFn iFn = (IFn)const__9.getRawRoot();
        IPersistentMap iPersistentMap2 = m_10313;
        m_10313 = null;
        Object object3 = msec_10315;
        msec_10315 = null;
        Object object4 = ((IFn)const__4.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__10, object3, (Object)const__5, (Object)const__11);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object5 = result__8982__auto__10336;
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
            Object object7 = result__8982__auto__10336;
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
        Object endmsg__8984__auto__10333 = iFn.invoke(object4, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.cassandra-values-v4");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object9 = endmsg__8984__auto__10333;
            endmsg__8984__auto__10333 = null;
            logger4.debug((String)((IFn)const__3.getRawRoot()).invoke(object9));
        }
        Object object10 = ((IFn)const__14.getRawRoot()).invoke(result__8982__auto__10336, (Object)const__15);
        if (object10 != null && object10 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object11 = result__8982__auto__10336;
            result__8982__auto__10336 = null;
            object2 = iLookupThunk3.get(object11);
            if (iLookupThunk3 == object2) {
                __thunk__2__ = __site__2__.fault(object11);
                object2 = __thunk__2__.get(object11);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object12 = result__8982__auto__10336;
            result__8982__auto__10336 = null;
            Object object13 = iLookupThunk4.get(object12);
            if (iLookupThunk4 == object13) {
                __thunk__3__ = __site__3__.fault(object12);
                object13 = __thunk__3__.get(object12);
            }
            throw (Throwable)object13;
        }
        Object object14 = temp__5457__auto__10339 = object2;
        if (object14 != null && object14 != Boolean.FALSE) {
            Object object15;
            Object and__5236__auto__10337;
            Object map__10318;
            Object object16;
            Object object17 = temp__5457__auto__10339;
            temp__5457__auto__10339 = null;
            Object map__103182 = object17;
            Object object18 = ((IFn)const__16.getRawRoot()).invoke(map__103182);
            if (object18 != null && object18 != Boolean.FALSE) {
                Object object19 = map__103182;
                map__103182 = null;
                object16 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__17.getRawRoot()).invoke(object19)));
            } else {
                object16 = map__103182;
                map__103182 = null;
            }
            Object ret = map__10318 = object16;
            Object chunks = RT.get((Object)map__10318, (Object)const__19);
            Object object20 = map__10318;
            map__10318 = null;
            Object val = RT.get((Object)object20, (Object)const__20);
            ILookupThunk iLookupThunk5 = __thunk__4__;
            Object object21 = ret;
            Object object22 = iLookupThunk5.get(object21);
            if (iLookupThunk5 == object22) {
                __thunk__4__ = __site__4__.fault(object21);
                object22 = __thunk__4__.get(object21);
            }
            Object object23 = and__5236__auto__10337 = object22;
            if (object23 != null && object23 != Boolean.FALSE) {
                IFn iFn3 = (IFn)const__22.getRawRoot();
                ILookupThunk iLookupThunk6 = __thunk__5__;
                Object object24 = ret;
                Object object25 = iLookupThunk6.get(object24);
                if (iLookupThunk6 == object25) {
                    __thunk__5__ = __site__5__.fault(object24);
                    object25 = __thunk__5__.get(object24);
                }
                object15 = iFn3.invoke(object25);
            } else {
                object15 = and__5236__auto__10337;
                and__5236__auto__10337 = null;
            }
            Object m = object15;
            Object object26 = ret;
            ret = null;
            Object object27 = m;
            m = null;
            Object ret2 = ((IFn)const__9.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke(object26, (Object)const__21, (Object)const__20, (Object)const__19), object27);
            if (Util.equiv((Object)chunks, (long)1L)) {
                Object and__5236__auto__10338;
                Object object28 = and__5236__auto__10338 = val;
                if (object28 != null && object28 != Boolean.FALSE) {
                    Object object29 = ret2;
                    ret2 = null;
                    Object object30 = val;
                    val = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object29, (Object)const__26, object30);
                } else {
                    object = and__5236__auto__10338;
                    and__5236__auto__10338 = null;
                }
            } else {
                Object object31 = val;
                val = null;
                Object object32 = id;
                id = null;
                Object object33 = session;
                session = null;
                Object object34 = table;
                table = null;
                Object object35 = chunks;
                chunks = null;
                Object results = ((IFn)const__27.getRawRoot()).invoke((Object)Tuple.create((Object)object31), ((IFn)const__28.getRawRoot()).invoke((Object)new cassandra_values_v4$get_value$fn__10319(object32, object33, object34), ((IFn)const__29.getRawRoot()).invoke(const__25, object35)));
                Object object36 = ((IFn)const__30.getRawRoot()).invoke(const__31.getRawRoot(), results);
                if (object36 != null && object36 != Boolean.FALSE) {
                    Object object37 = ret2;
                    ret2 = null;
                    Object object38 = results;
                    results = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object37, (Object)const__26, ((IFn)const__32.getRawRoot()).invoke(object38));
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
        return cassandra_values_v4$get_value.invokeStatic(object4, object5, object6);
    }
}

