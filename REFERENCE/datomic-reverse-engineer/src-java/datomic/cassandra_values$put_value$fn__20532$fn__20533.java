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
import datomic.cassandra_values$put_value$fn__20532$fn__20533$fn__20537;
import java.nio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class cassandra_values$put_value$fn__20532$fn__20533
extends AFunction {
    Object id;
    Object chunks;
    Object table;
    Object session;
    Object val_map;
    Object n;
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"cassandra-values", (String)"put-value-chunk");
    public static final Keyword const__2 = RT.keyword(null, (String)"id");
    public static final Keyword const__3 = RT.keyword(null, (String)"n");
    public static final Keyword const__4 = RT.keyword(null, (String)"bufsize");
    public static final Var const__6 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__8 = RT.keyword(null, (String)"phase");
    public static final Keyword const__9 = RT.keyword(null, (String)"begin");
    public static final Var const__11 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__13 = RT.keyword(null, (String)"msec");
    public static final Keyword const__14 = RT.keyword(null, (String)"end");
    public static final Keyword const__15 = RT.keyword(null, (String)"threw");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__18 = RT.keyword(null, (String)"returned");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public cassandra_values$put_value$fn__20532$fn__20533(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.id = object;
        this.chunks = object2;
        this.table = object3;
        this.session = object4;
        this.val_map = object5;
        this.n = object6;
    }

    public Object invoke() {
        Object object;
        IPersistentMap iPersistentMap;
        Object[] objectArray = new Object[8];
        objectArray[0] = const__0;
        objectArray[1] = const__1;
        objectArray[2] = const__2;
        objectArray[3] = this.id = null;
        objectArray[4] = const__3;
        objectArray[5] = this.n;
        objectArray[6] = const__4;
        this.chunks = null;
        this.n = null;
        objectArray[7] = ((Buffer)RT.nth((Object)this.chunks, (int)RT.intCast((Object)((Number)this.n)))).remaining();
        IPersistentMap m_20534 = RT.mapUniqueKeys((Object[])objectArray);
        Logger logger = LoggerFactory.getLogger((String)"datomic.cassandra-values");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)m_20534, (Object)const__8, (Object)const__9)));
        }
        long start__8981__auto__20544 = System.nanoTime();
        this.table = null;
        this.session = null;
        this.val_map = null;
        Object result__8982__auto__20545 = ((IFn)new cassandra_values$put_value$fn__20532$fn__20533$fn__20537(this.table, this.session, this.val_map)).invoke();
        long elapsed_20535 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__20544);
        Object msec_20536 = ((IFn)const__11.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_20535));
        IFn iFn = (IFn)const__12.getRawRoot();
        IPersistentMap iPersistentMap2 = m_20534;
        m_20534 = null;
        Object object2 = msec_20536;
        msec_20536 = null;
        Object object3 = ((IFn)const__7.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__13, object2, (Object)const__8, (Object)const__14);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object4 = result__8982__auto__20545;
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__0__ = __site__0__.fault(object4);
            object5 = __thunk__0__.get(object4);
        }
        if (object5 != null && object5 != Boolean.FALSE) {
            Object[] objectArray2 = new Object[2];
            objectArray2[0] = const__15;
            IFn iFn2 = (IFn)const__16.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object6 = result__8982__auto__20545;
            Object object7 = iLookupThunk2.get(object6);
            if (iLookupThunk2 == object7) {
                __thunk__1__ = __site__1__.fault(object6);
                object7 = __thunk__1__.get(object6);
            }
            objectArray2[1] = iFn2.invoke(object7);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__20542 = iFn.invoke(object3, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.cassandra-values");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object8 = endmsg__8984__auto__20542;
            endmsg__8984__auto__20542 = null;
            logger4.debug((String)((IFn)const__6.getRawRoot()).invoke(object8));
        }
        Object object9 = ((IFn)const__17.getRawRoot()).invoke(result__8982__auto__20545, (Object)const__18);
        if (object9 != null && object9 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object10 = result__8982__auto__20545;
            result__8982__auto__20545 = null;
            object = iLookupThunk3.get(object10);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object10);
                object = __thunk__2__.get(object10);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object11 = result__8982__auto__20545;
            result__8982__auto__20545 = null;
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

