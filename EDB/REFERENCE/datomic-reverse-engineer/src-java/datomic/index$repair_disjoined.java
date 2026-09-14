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
import datomic.index$repair_disjoined$fn__15707;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$repair_disjoined
extends AFunction {
    public static final Keyword const__1 = RT.keyword(null, (String)"buildRevision");
    public static final Object const__2 = 0L;
    public static final Keyword const__4 = RT.keyword(null, (String)"event");
    public static final Keyword const__5 = RT.keyword((String)"index", (String)"repair-disjoined-db");
    public static final Keyword const__6 = RT.keyword(null, (String)"root-map");
    public static final Var const__7 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__9 = RT.keyword(null, (String)"phase");
    public static final Keyword const__10 = RT.keyword(null, (String)"begin");
    public static final Var const__12 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__14 = RT.keyword(null, (String)"msec");
    public static final Keyword const__15 = RT.keyword(null, (String)"end");
    public static final Keyword const__16 = RT.keyword(null, (String)"threw");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__19 = RT.keyword(null, (String)"returned");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object db2, Object olookup, Object root_map) {
        IPersistentMap iPersistentMap;
        Object object;
        Object object2;
        Object or__5238__auto__15731;
        Object object3 = or__5238__auto__15731 = ((IFn)root_map).invoke((Object)const__1);
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = or__5238__auto__15731;
            or__5238__auto__15731 = null;
        } else {
            object2 = const__2;
        }
        if (!Numbers.lt((Object)object2, (long)4846L)) {
            object = db2;
            return object;
        }
        Object[] objectArray = new Object[4];
        objectArray[0] = const__4;
        objectArray[1] = const__5;
        objectArray[2] = const__6;
        Object object4 = root_map;
        root_map = null;
        objectArray[3] = object4;
        IPersistentMap m_15704 = RT.mapUniqueKeys((Object[])objectArray);
        Logger logger = LoggerFactory.getLogger((String)"datomic.index");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)m_15704, (Object)const__9, (Object)const__10)));
        }
        long start__8981__auto__15734 = System.nanoTime();
        Object object5 = db2;
        db2 = null;
        Object result__8982__auto__15735 = ((IFn)new index$repair_disjoined$fn__15707(object5)).invoke();
        long elapsed_15705 = System.nanoTime() - start__8981__auto__15734;
        Object msec_15706 = ((IFn)const__12.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_15705));
        IFn iFn = (IFn)const__13.getRawRoot();
        IPersistentMap iPersistentMap2 = m_15704;
        m_15704 = null;
        Object object6 = msec_15706;
        msec_15706 = null;
        Object object7 = ((IFn)const__8.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__14, object6, (Object)const__9, (Object)const__15);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object8 = result__8982__auto__15735;
        Object object9 = iLookupThunk.get(object8);
        if (iLookupThunk == object9) {
            __thunk__0__ = __site__0__.fault(object8);
            object9 = __thunk__0__.get(object8);
        }
        if (object9 != null && object9 != Boolean.FALSE) {
            Object[] objectArray2 = new Object[2];
            objectArray2[0] = const__16;
            IFn iFn2 = (IFn)const__17.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object10 = result__8982__auto__15735;
            Object object11 = iLookupThunk2.get(object10);
            if (iLookupThunk2 == object11) {
                __thunk__1__ = __site__1__.fault(object10);
                object11 = __thunk__1__.get(object10);
            }
            objectArray2[1] = iFn2.invoke(object11);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__15732 = iFn.invoke(object7, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.index");
        if (logger3.isInfoEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object12 = endmsg__8984__auto__15732;
            endmsg__8984__auto__15732 = null;
            logger4.info((String)((IFn)const__7.getRawRoot()).invoke(object12));
        }
        Object object13 = ((IFn)const__18.getRawRoot()).invoke(result__8982__auto__15735, (Object)const__19);
        if (object13 != null && object13 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object14 = result__8982__auto__15735;
            result__8982__auto__15735 = null;
            object = iLookupThunk3.get(object14);
            if (iLookupThunk3 != object) {
                return object;
            }
            __thunk__2__ = __site__2__.fault(object14);
            object = __thunk__2__.get(object14);
            return object;
        }
        ILookupThunk iLookupThunk4 = __thunk__3__;
        Object object15 = result__8982__auto__15735;
        result__8982__auto__15735 = null;
        Object object16 = iLookupThunk4.get(object15);
        if (iLookupThunk4 != object16) {
            throw (Throwable)object16;
        }
        __thunk__3__ = __site__3__.fault(object15);
        object16 = __thunk__3__.get(object15);
        throw (Throwable)object16;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$repair_disjoined.invokeStatic(object4, object5, object6);
    }
}

