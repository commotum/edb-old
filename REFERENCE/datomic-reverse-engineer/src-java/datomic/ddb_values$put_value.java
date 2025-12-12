/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.ddb_values$put_value$fn__20392;
import datomic.ddb_values$put_value$fn__20394;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ddb_values$put_value
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
    public static final Keyword const__4 = RT.keyword(null, (String)"v");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__6 = RT.var((String)"datomic.io", (String)"bbuf->base128");
    public static final Var const__7 = RT.var((String)"datomic.ddb-values", (String)"chunk");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"atom");
    public static final Keyword const__13 = RT.keyword(null, (String)"event");
    public static final Keyword const__14 = RT.keyword((String)"ddb-values", (String)"put-value");
    public static final Keyword const__15 = RT.keyword(null, (String)"bufsize");
    public static final Var const__18 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__20 = RT.keyword(null, (String)"phase");
    public static final Keyword const__21 = RT.keyword(null, (String)"begin");
    public static final Var const__23 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__25 = RT.keyword(null, (String)"msec");
    public static final Keyword const__26 = RT.keyword(null, (String)"end");
    public static final Keyword const__27 = RT.keyword(null, (String)"threw");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__30 = RT.keyword(null, (String)"returned");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__33 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__34 = RT.var((String)"clojure.core", (String)"range");
    public static final Object const__35 = 1L;
    public static final Keyword const__36 = RT.keyword((String)"ddb-values", (String)"put");
    public static final Keyword const__37 = RT.keyword(null, (String)"sdkResponseMetadata");
    public static final Keyword const__38 = RT.keyword(null, (String)"bbuf-size");
    public static final Var const__39 = RT.var((String)"datomic.io", (String)"remaining");
    public static final Keyword const__40 = RT.keyword(null, (String)"size");
    public static final Keyword const__41 = RT.keyword(null, (String)"chunks");
    public static final Keyword const__42 = RT.keyword(null, (String)"created");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object ddb_client, Object table, Object value) {
        block12: {
            Object object;
            IPersistentMap iPersistentMap;
            Object object2;
            Object map__20388 = value;
            Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__20388);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = map__20388;
                map__20388 = null;
                object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
            } else {
                object2 = map__20388;
                map__20388 = null;
            }
            Object map__203882 = object2;
            Object id = RT.get((Object)map__203882, (Object)const__3);
            Object object5 = map__203882;
            map__203882 = null;
            Object v = RT.get((Object)object5, (Object)const__4);
            Object object6 = value;
            value = null;
            Object base = ((IFn)const__5.getRawRoot()).invoke(object6, (Object)const__3, (Object)const__4);
            Object body = ((IFn)const__6.getRawRoot()).invoke(v);
            Object chunks = ((IFn)const__7.getRawRoot()).invoke(body, (Object)Numbers.num((long)Numbers.multiply((long)62L, (long)1024L)));
            int n = RT.count((Object)chunks);
            IFn iFn = (IFn)const__12.getRawRoot();
            IPersistentMap m_20389 = RT.mapUniqueKeys((Object[])new Object[]{const__13, const__14, const__3, id, const__15, RT.count((Object)RT.nth((Object)chunks, (int)RT.intCast((long)0L)))});
            Logger logger = LoggerFactory.getLogger((String)"datomic.ddb-values");
            if (logger.isDebugEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.debug((String)((IFn)const__18.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke((Object)m_20389, (Object)const__20, (Object)const__21)));
            }
            long start__8981__auto__20414 = System.nanoTime();
            Object object7 = base;
            base = null;
            Object result__8982__auto__20415 = ((IFn)new ddb_values$put_value$fn__20392(n, chunks, id, table, ddb_client, object7)).invoke();
            long elapsed_20390 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__20414);
            Object msec_20391 = ((IFn)const__23.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_20390));
            IFn iFn2 = (IFn)const__24.getRawRoot();
            IPersistentMap iPersistentMap2 = m_20389;
            m_20389 = null;
            Object object8 = msec_20391;
            msec_20391 = null;
            Object object9 = ((IFn)const__19.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__25, object8, (Object)const__20, (Object)const__26);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object10 = result__8982__auto__20415;
            Object object11 = iLookupThunk.get(object10);
            if (iLookupThunk == object11) {
                __thunk__0__ = __site__0__.fault(object10);
                object11 = __thunk__0__.get(object10);
            }
            if (object11 != null && object11 != Boolean.FALSE) {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__27;
                IFn iFn3 = (IFn)const__28.getRawRoot();
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object12 = result__8982__auto__20415;
                Object object13 = iLookupThunk2.get(object12);
                if (iLookupThunk2 == object13) {
                    __thunk__1__ = __site__1__.fault(object12);
                    object13 = __thunk__1__.get(object12);
                }
                objectArray[1] = iFn3.invoke(object13);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                iPersistentMap = null;
            }
            Object endmsg__8984__auto__20412 = iFn2.invoke(object9, iPersistentMap);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.ddb-values");
            if (logger3.isDebugEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                Object object14 = endmsg__8984__auto__20412;
                endmsg__8984__auto__20412 = null;
                logger4.debug((String)((IFn)const__18.getRawRoot()).invoke(object14));
            }
            Object object15 = ((IFn)const__29.getRawRoot()).invoke(result__8982__auto__20415, (Object)const__30);
            if (object15 != null && object15 != Boolean.FALSE) {
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object16 = result__8982__auto__20415;
                result__8982__auto__20415 = null;
                object = iLookupThunk3.get(object16);
                if (iLookupThunk3 == object) {
                    __thunk__2__ = __site__2__.fault(object16);
                    object = __thunk__2__.get(object16);
                }
            } else {
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object17 = result__8982__auto__20415;
                result__8982__auto__20415 = null;
                Object object18 = iLookupThunk4.get(object17);
                if (iLookupThunk4 == object18) {
                    __thunk__3__ = __site__3__.fault(object17);
                    object18 = __thunk__3__.get(object17);
                }
                throw (Throwable)object18;
            }
            IPersistentVector rets = Tuple.create((Object)iFn.invoke(object));
            Object object19 = chunks;
            chunks = null;
            Object object20 = table;
            table = null;
            Object object21 = ddb_client;
            ddb_client = null;
            IPersistentVector iPersistentVector = rets;
            rets = null;
            Object rets2 = ((IFn)const__31.getRawRoot()).invoke(const__32.getRawRoot(), ((IFn)const__33.getRawRoot()).invoke((Object)new ddb_values$put_value$fn__20394(object19, id, object20, object21), (Object)iPersistentVector, ((IFn)const__34.getRawRoot()).invoke(const__35, (Object)n)));
            Logger logger5 = LoggerFactory.getLogger((String)"datomic.ddb-values");
            if (!logger5.isInfoEnabled()) break block12;
            Logger logger6 = logger5;
            logger5 = null;
            Object[] objectArray = new Object[10];
            objectArray[0] = const__36;
            Object object22 = rets2;
            rets2 = null;
            objectArray[1] = ((IFn)const__31.getRawRoot()).invoke((Object)const__37, object22);
            objectArray[2] = const__3;
            Object object23 = id;
            id = null;
            objectArray[3] = object23;
            objectArray[4] = const__38;
            Object object24 = v;
            v = null;
            objectArray[5] = Numbers.num((long)((IFn.OL)const__39.getRawRoot()).invokePrim(object24));
            objectArray[6] = const__40;
            Object object25 = body;
            body = null;
            objectArray[7] = RT.count((Object)object25);
            objectArray[8] = const__41;
            objectArray[9] = n;
            logger6.info((String)((IFn)const__18.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
        }
        return const__42;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return ddb_values$put_value.invokeStatic(object4, object5, object6);
    }
}

