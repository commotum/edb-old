/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
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
import datomic.integrity$crosscheck_log$fn__22145;
import datomic.integrity$crosscheck_log$fn__22147;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class integrity$crosscheck_log
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.integrity", (String)"crosscheck-log");
    public static final Keyword const__1 = RT.keyword(null, (String)"event");
    public static final Keyword const__2 = RT.keyword((String)"integrity", (String)"crosscheck-log");
    public static final Keyword const__3 = RT.keyword(null, (String)"index");
    public static final Keyword const__4 = RT.keyword(null, (String)"db");
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
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__20 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__21 = RT.var((String)"datomic.log", (String)"seek-tx");
    public static final Object const__22 = 0L;
    public static final Var const__23 = RT.var((String)"datomic.integrity", (String)"make-tupler");
    public static final Var const__24 = RT.var((String)"datomic.tools", (String)"ever-nohistory-attrs");
    public static final Var const__25 = RT.var((String)"datomic.api", (String)"basis-t");
    public static final Var const__26 = RT.var((String)"datomic.api", (String)"t->tx");
    public static final Var const__27 = RT.var((String)"datomic.api", (String)"history");
    public static final Var const__28 = RT.var((String)"datomic.integrity", (String)"mk-index-pred");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"reduce");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public static Object invokeStatic(Object log2, Object db2, Object index2, Object progress) {
        Object object;
        Object temp__5455__auto__22164;
        IPersistentMap iPersistentMap;
        Object[] objectArray = new Object[6];
        objectArray[0] = const__1;
        objectArray[1] = const__2;
        objectArray[2] = const__3;
        objectArray[3] = index2;
        objectArray[4] = const__4;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = db2;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        objectArray[5] = object3;
        IPersistentMap m_22142 = RT.mapUniqueKeys((Object[])objectArray);
        Logger logger = LoggerFactory.getLogger((String)"datomic.integrity");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)m_22142, (Object)const__8, (Object)const__9)));
        }
        long start__8981__auto__22162 = System.nanoTime();
        Object result__8982__auto__22163 = ((IFn)new integrity$crosscheck_log$fn__22145()).invoke();
        long elapsed_22143 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__22162);
        Object msec_22144 = ((IFn)const__11.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_22143));
        IFn iFn = (IFn)const__12.getRawRoot();
        IPersistentMap iPersistentMap2 = m_22142;
        m_22142 = null;
        Object object4 = msec_22144;
        msec_22144 = null;
        Object object5 = ((IFn)const__7.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__13, object4, (Object)const__8, (Object)const__14);
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object6 = result__8982__auto__22163;
        Object object7 = iLookupThunk2.get(object6);
        if (iLookupThunk2 == object7) {
            __thunk__1__ = __site__1__.fault(object6);
            object7 = __thunk__1__.get(object6);
        }
        if (object7 != null && object7 != Boolean.FALSE) {
            Object[] objectArray2 = new Object[2];
            objectArray2[0] = const__15;
            IFn iFn2 = (IFn)const__16.getRawRoot();
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object8 = result__8982__auto__22163;
            Object object9 = iLookupThunk3.get(object8);
            if (iLookupThunk3 == object9) {
                __thunk__2__ = __site__2__.fault(object8);
                object9 = __thunk__2__.get(object8);
            }
            objectArray2[1] = iFn2.invoke(object9);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__22160 = iFn.invoke(object5, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.integrity");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object10 = endmsg__8984__auto__22160;
            endmsg__8984__auto__22160 = null;
            logger4.debug((String)((IFn)const__6.getRawRoot()).invoke(object10));
        }
        Object object11 = ((IFn)const__17.getRawRoot()).invoke(result__8982__auto__22163, (Object)const__18);
        if (object11 != null && object11 != Boolean.FALSE) {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object12 = result__8982__auto__22163;
            result__8982__auto__22163 = null;
            Object object13 = iLookupThunk4.get(object12);
            if (iLookupThunk4 == object13) {
                __thunk__3__ = __site__3__.fault(object12);
                object13 = __thunk__3__.get(object12);
            }
        } else {
            ILookupThunk iLookupThunk5 = __thunk__4__;
            Object object14 = result__8982__auto__22163;
            result__8982__auto__22163 = null;
            Object object15 = iLookupThunk5.get(object14);
            if (iLookupThunk5 == object15) {
                __thunk__4__ = __site__4__.fault(object14);
                object15 = __thunk__4__.get(object14);
            }
            throw (Throwable)object15;
        }
        Object object16 = log2;
        log2 = null;
        Object object17 = temp__5455__auto__22164 = ((IFn)const__19.getRawRoot()).invoke(((IFn)const__20.getRawRoot()).invoke(((IFn)const__21.getRawRoot()).invoke(object16, const__22)));
        if (object17 != null && object17 != Boolean.FALSE) {
            Object object18 = temp__5455__auto__22164;
            temp__5455__auto__22164 = null;
            Object log_seq = object18;
            Object tupler = ((IFn)const__23.getRawRoot()).invoke(index2);
            Object nohists = ((IFn)const__24.getRawRoot()).invoke(db2);
            Object basis_t2 = ((IFn)const__25.getRawRoot()).invoke(db2);
            Object limit_tx = ((IFn.LO)const__26.getRawRoot()).invokePrim(RT.longCast((Object)((Number)basis_t2)));
            Object hist = ((IFn)const__27.getRawRoot()).invoke(db2);
            Object index_pred = ((IFn)const__28.getRawRoot()).invoke(db2, index2);
            Object object19 = limit_tx;
            limit_tx = null;
            Object object20 = index_pred;
            index_pred = null;
            Object object21 = index2;
            index2 = null;
            Object object22 = db2;
            db2 = null;
            Object object23 = tupler;
            tupler = null;
            Object object24 = hist;
            hist = null;
            Object object25 = progress;
            progress = null;
            Object object26 = basis_t2;
            basis_t2 = null;
            Object object27 = nohists;
            nohists = null;
            Object object28 = log_seq;
            log_seq = null;
            object = ((IFn)const__29.getRawRoot()).invoke((Object)new integrity$crosscheck_log$fn__22147(object19, object20, object21, object22, object23, object24, object25, object26, object27), const__22, object28);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return integrity$crosscheck_log.invokeStatic(object5, object6, object7, object8);
    }

    public static Object invokeStatic(Object log2, Object db2, Object index2) {
        Object object = log2;
        log2 = null;
        Object object2 = db2;
        db2 = null;
        Object object3 = index2;
        index2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, null);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return integrity$crosscheck_log.invokeStatic(object4, object5, object6);
    }
}

