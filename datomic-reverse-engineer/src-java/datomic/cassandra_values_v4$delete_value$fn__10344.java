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
import clojure.lang.Var;
import datomic.cassandra_values_v4$delete_value$fn__10344$fn__10348;
import datomic.cassandra_values_v4$delete_value$fn__10344$fn__10351;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class cassandra_values_v4$delete_value$fn__10344
extends AFunction {
    Object id;
    Object table;
    Object session;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Keyword const__1 = RT.keyword(null, (String)"event");
    public static final Keyword const__2 = RT.keyword((String)"cassandra-values", (String)"get-value");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
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
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__19 = RT.keyword(null, (String)"chunks");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"dorun");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"range");
    public static final Object const__23 = 1L;
    public static final Var const__24 = RT.var((String)"datomic.cassandra-v4", (String)"cql-delete");
    public static final Var const__25 = RT.var((String)"datomic.cassandra-values-v4", (String)"cql-keys");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public cassandra_values_v4$delete_value$fn__10344(Object object, Object object2, Object object3) {
        this.id = object;
        this.table = object2;
        this.session = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object object;
            Object temp__5457__auto__10367;
            Object object2;
            IPersistentMap iPersistentMap2;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            IPersistentMap m_10345 = RT.mapUniqueKeys((Object[])new Object[]{const__1, const__2, const__3, this.id});
            Logger logger = LoggerFactory.getLogger((String)"datomic.cassandra-values-v4");
            if (logger.isDebugEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.debug((String)((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)m_10345, (Object)const__6, (Object)const__7)));
            }
            long start__8981__auto__10365 = System.nanoTime();
            Object result__8982__auto__10366 = ((IFn)new cassandra_values_v4$delete_value$fn__10344$fn__10348(this.id, this.table, this.session)).invoke();
            long elapsed_10346 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__10365);
            Object msec_10347 = ((IFn)const__9.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_10346));
            IFn iFn = (IFn)const__10.getRawRoot();
            IPersistentMap iPersistentMap3 = m_10345;
            m_10345 = null;
            Object object3 = msec_10347;
            msec_10347 = null;
            Object object4 = ((IFn)const__5.getRawRoot()).invoke((Object)iPersistentMap3, (Object)const__11, object3, (Object)const__6, (Object)const__12);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object5 = result__8982__auto__10366;
            Object object6 = iLookupThunk.get(object5);
            if (iLookupThunk == object6) {
                __thunk__0__ = __site__0__.fault(object5);
                object6 = __thunk__0__.get(object5);
            }
            if (object6 != null && object6 != Boolean.FALSE) {
                Object[] objectArray2 = new Object[2];
                objectArray2[0] = const__13;
                IFn iFn2 = (IFn)const__14.getRawRoot();
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object7 = result__8982__auto__10366;
                Object object8 = iLookupThunk2.get(object7);
                if (iLookupThunk2 == object8) {
                    __thunk__1__ = __site__1__.fault(object7);
                    object8 = __thunk__1__.get(object7);
                }
                objectArray2[1] = iFn2.invoke(object8);
                iPersistentMap2 = RT.mapUniqueKeys((Object[])objectArray2);
            } else {
                iPersistentMap2 = null;
            }
            Object endmsg__8984__auto__10363 = iFn.invoke(object4, iPersistentMap2);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.cassandra-values-v4");
            if (logger3.isDebugEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                Object object9 = endmsg__8984__auto__10363;
                endmsg__8984__auto__10363 = null;
                logger4.debug((String)((IFn)const__4.getRawRoot()).invoke(object9));
            }
            Object object10 = ((IFn)const__15.getRawRoot()).invoke(result__8982__auto__10366, (Object)const__0);
            if (object10 != null && object10 != Boolean.FALSE) {
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object11 = result__8982__auto__10366;
                result__8982__auto__10366 = null;
                object2 = iLookupThunk3.get(object11);
                if (iLookupThunk3 == object2) {
                    __thunk__2__ = __site__2__.fault(object11);
                    object2 = __thunk__2__.get(object11);
                }
            } else {
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object12 = result__8982__auto__10366;
                result__8982__auto__10366 = null;
                Object object13 = iLookupThunk4.get(object12);
                if (iLookupThunk4 == object13) {
                    __thunk__3__ = __site__3__.fault(object12);
                    object13 = __thunk__3__.get(object12);
                }
                throw (Throwable)object13;
            }
            Object object14 = temp__5457__auto__10367 = object2;
            if (object14 != null && object14 != Boolean.FALSE) {
                Object chunks;
                Object map__10350;
                Object object15;
                Object object16 = temp__5457__auto__10367;
                temp__5457__auto__10367 = null;
                Object map__103502 = object16;
                Object object17 = ((IFn)const__16.getRawRoot()).invoke(map__103502);
                if (object17 != null && object17 != Boolean.FALSE) {
                    Object object18 = map__103502;
                    map__103502 = null;
                    object15 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__17.getRawRoot()).invoke(object18)));
                } else {
                    object15 = map__103502;
                    map__103502 = null;
                }
                Object object19 = map__10350 = object15;
                map__10350 = null;
                Object object20 = chunks = RT.get((Object)object19, (Object)const__19);
                chunks = null;
                ((IFn)const__20.getRawRoot()).invoke(((IFn)const__21.getRawRoot()).invoke((Object)new cassandra_values_v4$delete_value$fn__10344$fn__10351(this.id, this.table, this.session), ((IFn)const__22.getRawRoot()).invoke(const__23, object20)));
                this.session = null;
                this.table = null;
                this.id = null;
                object = ((IFn)const__24.getRawRoot()).invoke(this.session, this.table, this.id, const__25.getRawRoot());
            } else {
                object = null;
            }
            objectArray[1] = object;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__13;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

