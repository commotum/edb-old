/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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
import datomic.cassandra_values_v4$put_value$fn__10283;
import datomic.cassandra_values_v4$put_value$fn__10288;
import datomic.cassandra_values_v4$put_value$fn__10290;
import java.nio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class cassandra_values_v4$put_value
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
    public static final Keyword const__4 = RT.keyword(null, (String)"rev");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__8 = RT.var((String)"datomic.io", (String)"chunk");
    public static final Object const__9 = 358400L;
    public static final Keyword const__11 = RT.keyword(null, (String)"chunks");
    public static final Keyword const__12 = RT.keyword(null, (String)"id2");
    public static final Keyword const__13 = RT.keyword(null, (String)"map");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Keyword const__15 = RT.keyword(null, (String)"val");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"atom");
    public static final Keyword const__19 = RT.keyword(null, (String)"event");
    public static final Keyword const__20 = RT.keyword((String)"cassandra-values", (String)"put-value");
    public static final Keyword const__21 = RT.keyword(null, (String)"bufsize");
    public static final Var const__22 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__24 = RT.keyword(null, (String)"phase");
    public static final Keyword const__25 = RT.keyword(null, (String)"begin");
    public static final Var const__27 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__29 = RT.keyword(null, (String)"msec");
    public static final Keyword const__30 = RT.keyword(null, (String)"end");
    public static final Keyword const__31 = RT.keyword(null, (String)"threw");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__33 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__34 = RT.keyword(null, (String)"returned");
    public static final Var const__35 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__36 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__37 = RT.var((String)"clojure.core", (String)"range");
    public static final Object const__38 = 1L;
    public static final Keyword const__39 = RT.keyword((String)"cassandra-values", (String)"put");
    public static final Keyword const__40 = RT.keyword(null, (String)"size");
    public static final Keyword const__41 = RT.keyword(null, (String)"created");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object session, Object table, Object p__10281) {
        block14: {
            Object object;
            IPersistentMap iPersistentMap;
            Object object2;
            Object map__10282;
            Object object3;
            Object object4 = p__10281;
            p__10281 = null;
            Object map__102822 = object4;
            Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__102822);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__102822;
                map__102822 = null;
                object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
            } else {
                object3 = map__102822;
                map__102822 = null;
            }
            Object v_map = map__10282 = object3;
            Object id = RT.get((Object)map__10282, (Object)const__3);
            Object rev = RT.get((Object)map__10282, (Object)const__4);
            Object object7 = map__10282;
            map__10282 = null;
            Object v = RT.get((Object)object7, (Object)const__5);
            Object object8 = v_map;
            v_map = null;
            Object m = ((IFn)const__6.getRawRoot()).invoke(object8, (Object)const__3, (Object)const__4, (Object)const__5);
            Object chunks = ((IFn)const__7.getRawRoot()).invoke((Object)new cassandra_values_v4$put_value$fn__10283(), ((IFn)const__8.getRawRoot()).invoke(v, const__9));
            int n = RT.count((Object)chunks);
            Object[] objectArray = new Object[10];
            objectArray[0] = const__11;
            objectArray[1] = n;
            objectArray[2] = const__12;
            objectArray[3] = id;
            objectArray[4] = const__13;
            Integer n2 = RT.count((Object)m);
            if (n2 != null && n2 != Boolean.FALSE) {
                Object object9 = m;
                m = null;
                object2 = ((IFn)const__14.getRawRoot()).invoke(object9);
            } else {
                object2 = null;
            }
            objectArray[5] = object2;
            objectArray[6] = const__4;
            Object object10 = rev;
            rev = null;
            objectArray[7] = object10;
            objectArray[8] = const__15;
            objectArray[9] = RT.nth((Object)chunks, (int)RT.intCast((long)0L));
            IPersistentMap val_map = RT.mapUniqueKeys((Object[])objectArray);
            IFn iFn = (IFn)const__18.getRawRoot();
            IPersistentMap m_10285 = RT.mapUniqueKeys((Object[])new Object[]{const__19, const__20, const__3, id, const__21, ((Buffer)RT.nth((Object)chunks, (int)RT.intCast((long)0L))).remaining()});
            Logger logger = LoggerFactory.getLogger((String)"datomic.cassandra-values-v4");
            if (logger.isDebugEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.debug((String)((IFn)const__22.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke((Object)m_10285, (Object)const__24, (Object)const__25)));
            }
            long start__8981__auto__10310 = System.nanoTime();
            IPersistentMap iPersistentMap2 = val_map;
            val_map = null;
            Object result__8982__auto__10311 = ((IFn)new cassandra_values_v4$put_value$fn__10288(iPersistentMap2, session, table)).invoke();
            long elapsed_10286 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__10310);
            Object msec_10287 = ((IFn)const__27.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_10286));
            IFn iFn2 = (IFn)const__28.getRawRoot();
            IPersistentMap iPersistentMap3 = m_10285;
            m_10285 = null;
            Object object11 = msec_10287;
            msec_10287 = null;
            Object object12 = ((IFn)const__23.getRawRoot()).invoke((Object)iPersistentMap3, (Object)const__29, object11, (Object)const__24, (Object)const__30);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object13 = result__8982__auto__10311;
            Object object14 = iLookupThunk.get(object13);
            if (iLookupThunk == object14) {
                __thunk__0__ = __site__0__.fault(object13);
                object14 = __thunk__0__.get(object13);
            }
            if (object14 != null && object14 != Boolean.FALSE) {
                Object[] objectArray2 = new Object[2];
                objectArray2[0] = const__31;
                IFn iFn3 = (IFn)const__32.getRawRoot();
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object15 = result__8982__auto__10311;
                Object object16 = iLookupThunk2.get(object15);
                if (iLookupThunk2 == object16) {
                    __thunk__1__ = __site__1__.fault(object15);
                    object16 = __thunk__1__.get(object15);
                }
                objectArray2[1] = iFn3.invoke(object16);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
            } else {
                iPersistentMap = null;
            }
            Object endmsg__8984__auto__10308 = iFn2.invoke(object12, iPersistentMap);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.cassandra-values-v4");
            if (logger3.isDebugEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                Object object17 = endmsg__8984__auto__10308;
                endmsg__8984__auto__10308 = null;
                logger4.debug((String)((IFn)const__22.getRawRoot()).invoke(object17));
            }
            Object object18 = ((IFn)const__33.getRawRoot()).invoke(result__8982__auto__10311, (Object)const__34);
            if (object18 != null && object18 != Boolean.FALSE) {
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object19 = result__8982__auto__10311;
                result__8982__auto__10311 = null;
                object = iLookupThunk3.get(object19);
                if (iLookupThunk3 == object) {
                    __thunk__2__ = __site__2__.fault(object19);
                    object = __thunk__2__.get(object19);
                }
            } else {
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object20 = result__8982__auto__10311;
                result__8982__auto__10311 = null;
                Object object21 = iLookupThunk4.get(object20);
                if (iLookupThunk4 == object21) {
                    __thunk__3__ = __site__3__.fault(object20);
                    object21 = __thunk__3__.get(object20);
                }
                throw (Throwable)object21;
            }
            IPersistentVector rets = Tuple.create((Object)iFn.invoke(object));
            Object object22 = session;
            session = null;
            Object object23 = id;
            id = null;
            Object object24 = chunks;
            chunks = null;
            Object object25 = table;
            table = null;
            IPersistentVector iPersistentVector = rets;
            rets = null;
            Object rets2 = ((IFn)const__7.getRawRoot()).invoke(const__35.getRawRoot(), ((IFn)const__36.getRawRoot()).invoke((Object)new cassandra_values_v4$put_value$fn__10290(object22, object23, object24, object25), (Object)iPersistentVector, ((IFn)const__37.getRawRoot()).invoke(const__38, (Object)n)));
            Logger logger5 = LoggerFactory.getLogger((String)"datomic.cassandra-values-v4");
            if (!logger5.isInfoEnabled()) break block14;
            Logger logger6 = logger5;
            logger5 = null;
            Object[] objectArray3 = new Object[4];
            objectArray3[0] = const__39;
            Object object26 = rets2;
            rets2 = null;
            objectArray3[1] = object26;
            objectArray3[2] = const__40;
            Object object27 = v;
            v = null;
            objectArray3[3] = ((Buffer)object27).remaining();
            logger6.info((String)((IFn)const__22.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray3)));
        }
        return const__41;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cassandra_values_v4$put_value.invokeStatic(object4, object5, object6);
    }
}

