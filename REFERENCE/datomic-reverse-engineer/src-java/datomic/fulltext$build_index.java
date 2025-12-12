/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
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

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.fulltext$build_index$fn__14674;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class fulltext$build_index
extends AFunction {
    public static final AFn const__2 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"index", (String)"build-fulltext")});
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__5 = RT.keyword(null, (String)"phase");
    public static final Keyword const__6 = RT.keyword(null, (String)"begin");
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__9 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__10 = RT.keyword(null, (String)"CreateFulltextIndexMsec");
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

    public static Object invokeStatic(Object cstore, Object olookup, Object db2, Object aevt2, Object attrids, Object old_root_id, Object old_hist_id) {
        Object object;
        IPersistentMap iPersistentMap;
        AFn m_14671 = const__2;
        Logger logger = LoggerFactory.getLogger((String)"datomic.fulltext");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)m_14671, (Object)const__5, (Object)const__6)));
        }
        long start__8981__auto__14721 = System.nanoTime();
        Object object2 = olookup;
        olookup = null;
        Object object3 = cstore;
        cstore = null;
        Object object4 = aevt2;
        aevt2 = null;
        Object object5 = db2;
        db2 = null;
        Object object6 = old_hist_id;
        old_hist_id = null;
        Object object7 = attrids;
        attrids = null;
        Object object8 = old_root_id;
        old_root_id = null;
        Object result__8982__auto__14722 = ((IFn)new fulltext$build_index$fn__14674(object2, object3, object4, object5, object6, object7, object8)).invoke();
        long elapsed_14672 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__14721);
        Object msec_14673 = ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_14672));
        ((IFn)const__9.getRawRoot()).invoke((Object)const__10, msec_14673);
        IFn iFn = (IFn)const__11.getRawRoot();
        AFn aFn = m_14671;
        m_14671 = null;
        Object object9 = msec_14673;
        msec_14673 = null;
        Object object10 = ((IFn)const__4.getRawRoot()).invoke((Object)aFn, (Object)const__12, object9, (Object)const__5, (Object)const__13);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object11 = result__8982__auto__14722;
        Object object12 = iLookupThunk.get(object11);
        if (iLookupThunk == object12) {
            __thunk__0__ = __site__0__.fault(object11);
            object12 = __thunk__0__.get(object11);
        }
        if (object12 != null && object12 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__14;
            IFn iFn2 = (IFn)const__15.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object13 = result__8982__auto__14722;
            Object object14 = iLookupThunk2.get(object13);
            if (iLookupThunk2 == object14) {
                __thunk__1__ = __site__1__.fault(object13);
                object14 = __thunk__1__.get(object13);
            }
            objectArray[1] = iFn2.invoke(object14);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__14719 = iFn.invoke(object10, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.fulltext");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object15 = endmsg__8984__auto__14719;
            endmsg__8984__auto__14719 = null;
            logger4.debug((String)((IFn)const__3.getRawRoot()).invoke(object15));
        }
        Object object16 = ((IFn)const__16.getRawRoot()).invoke(result__8982__auto__14722, (Object)const__17);
        if (object16 != null && object16 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object17 = result__8982__auto__14722;
            result__8982__auto__14722 = null;
            object = iLookupThunk3.get(object17);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object17);
                object = __thunk__2__.get(object17);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object18 = result__8982__auto__14722;
            result__8982__auto__14722 = null;
            Object object19 = iLookupThunk4.get(object18);
            if (iLookupThunk4 == object19) {
                __thunk__3__ = __site__3__.fault(object18);
                object19 = __thunk__3__.get(object18);
            }
            throw (Throwable)object19;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        Object object8 = object;
        object = null;
        Object object9 = object2;
        object2 = null;
        Object object10 = object3;
        object3 = null;
        Object object11 = object4;
        object4 = null;
        Object object12 = object5;
        object5 = null;
        Object object13 = object6;
        object6 = null;
        Object object14 = object7;
        object7 = null;
        return fulltext$build_index.invokeStatic(object8, object9, object10, object11, object12, object13, object14);
    }
}

