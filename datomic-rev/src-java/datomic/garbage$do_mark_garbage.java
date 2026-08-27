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
 *  clojure.lang.PersistentVector
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
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.garbage$do_mark_garbage$fn__19798;
import datomic.garbage$do_mark_garbage$fn__19803;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class garbage$do_mark_garbage
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__1 = RT.keyword(null, (String)"GarbageSegments");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"conj");
    public static final Keyword const__6 = RT.keyword(null, (String)"tstamp");
    public static final Keyword const__7 = RT.keyword(null, (String)"vals");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__9 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__14 = RT.keyword(null, (String)"event");
    public static final Keyword const__15 = RT.keyword((String)"garbage", (String)"append-leaf");
    public static final Keyword const__16 = RT.keyword(null, (String)"segments");
    public static final Var const__17 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__19 = RT.keyword(null, (String)"phase");
    public static final Keyword const__20 = RT.keyword(null, (String)"begin");
    public static final Var const__22 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__24 = RT.keyword(null, (String)"msec");
    public static final Keyword const__25 = RT.keyword(null, (String)"end");
    public static final Keyword const__26 = RT.keyword(null, (String)"threw");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__29 = RT.keyword(null, (String)"returned");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"dissoc");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object m, Object cluster2, Object lookup, Object ids, Object max_leaf_size, Object max_dir_size) {
        Object object;
        Object object2;
        ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)RT.count((Object)ids));
        Object cluster_garbage = RT.get((Object)m, (Object)cluster2, (Object)PersistentVector.EMPTY);
        Object object3 = ((IFn)const__4.getRawRoot()).invoke(ids);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = cluster_garbage;
            cluster_garbage = null;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__6;
            objectArray[1] = new Date();
            objectArray[2] = const__7;
            Object object5 = ids;
            ids = null;
            objectArray[3] = ((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), object5);
            object2 = ((IFn)const__5.getRawRoot()).invoke(object4, (Object)RT.mapUniqueKeys((Object[])objectArray));
        } else {
            object2 = cluster_garbage;
            cluster_garbage = null;
        }
        Object cluster_garbage2 = object2;
        Object leaf_size = ((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), ((IFn)const__12.getRawRoot()).invoke((Object)new garbage$do_mark_garbage$fn__19798(), cluster_garbage2));
        Object object6 = max_leaf_size;
        max_leaf_size = null;
        if (Numbers.lt((Object)object6, (Object)leaf_size)) {
            IPersistentMap iPersistentMap;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__14;
            objectArray[1] = const__15;
            objectArray[2] = const__16;
            Object object7 = leaf_size;
            leaf_size = null;
            objectArray[3] = object7;
            IPersistentMap m_19800 = RT.mapUniqueKeys((Object[])objectArray);
            Logger logger = LoggerFactory.getLogger((String)"datomic.garbage");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.info((String)((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke((Object)m_19800, (Object)const__19, (Object)const__20)));
            }
            long start__8981__auto__19810 = System.nanoTime();
            Object object8 = max_dir_size;
            max_dir_size = null;
            Object object9 = lookup;
            lookup = null;
            Object object10 = cluster_garbage2;
            cluster_garbage2 = null;
            Object result__8982__auto__19811 = ((IFn)new garbage$do_mark_garbage$fn__19803(cluster2, object8, object9, object10)).invoke();
            long elapsed_19801 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__19810);
            Object msec_19802 = ((IFn)const__22.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_19801));
            IFn iFn = (IFn)const__23.getRawRoot();
            IPersistentMap iPersistentMap2 = m_19800;
            m_19800 = null;
            Object object11 = msec_19802;
            msec_19802 = null;
            Object object12 = ((IFn)const__18.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__24, object11, (Object)const__19, (Object)const__25);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object13 = result__8982__auto__19811;
            Object object14 = iLookupThunk.get(object13);
            if (iLookupThunk == object14) {
                __thunk__0__ = __site__0__.fault(object13);
                object14 = __thunk__0__.get(object13);
            }
            if (object14 != null && object14 != Boolean.FALSE) {
                Object[] objectArray2 = new Object[2];
                objectArray2[0] = const__26;
                IFn iFn2 = (IFn)const__27.getRawRoot();
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object15 = result__8982__auto__19811;
                Object object16 = iLookupThunk2.get(object15);
                if (iLookupThunk2 == object16) {
                    __thunk__1__ = __site__1__.fault(object15);
                    object16 = __thunk__1__.get(object15);
                }
                objectArray2[1] = iFn2.invoke(object16);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
            } else {
                iPersistentMap = null;
            }
            Object endmsg__8984__auto__19808 = iFn.invoke(object12, iPersistentMap);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.garbage");
            if (logger3.isInfoEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                Object object17 = endmsg__8984__auto__19808;
                endmsg__8984__auto__19808 = null;
                logger4.info((String)((IFn)const__17.getRawRoot()).invoke(object17));
            }
            Object object18 = ((IFn)const__28.getRawRoot()).invoke(result__8982__auto__19811, (Object)const__29);
            if (object18 != null && object18 != Boolean.FALSE) {
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object19 = result__8982__auto__19811;
                result__8982__auto__19811 = null;
                Object object20 = iLookupThunk3.get(object19);
                if (iLookupThunk3 == object20) {
                    __thunk__2__ = __site__2__.fault(object19);
                    object20 = __thunk__2__.get(object19);
                }
            } else {
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object21 = result__8982__auto__19811;
                result__8982__auto__19811 = null;
                Object object22 = iLookupThunk4.get(object21);
                if (iLookupThunk4 == object22) {
                    __thunk__3__ = __site__3__.fault(object21);
                    object22 = __thunk__3__.get(object21);
                }
                throw (Throwable)object22;
            }
            Object object23 = m;
            m = null;
            Object object24 = cluster2;
            cluster2 = null;
            object = ((IFn)const__30.getRawRoot()).invoke(object23, object24);
        } else {
            Object object25 = m;
            m = null;
            Object object26 = cluster2;
            cluster2 = null;
            Object object27 = cluster_garbage2;
            cluster_garbage2 = null;
            object = ((IFn)const__18.getRawRoot()).invoke(object25, object26, object27);
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return garbage$do_mark_garbage.invokeStatic(object7, object8, object9, object10, object11, object12);
    }
}

