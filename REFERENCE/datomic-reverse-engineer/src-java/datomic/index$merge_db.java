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
import datomic.db.Db;
import datomic.index$merge_db$fn__15867;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$merge_db
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"event");
    public static final Keyword const__1 = RT.keyword((String)"index", (String)"merge-db");
    public static final Keyword const__2 = RT.keyword(null, (String)"as-of-t");
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
    public static final Var const__16 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object cstore, Object olookup, Object db2, Object as_of_t2) {
        Object object;
        Object object2 = ((Db)db2).indexing;
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)new IllegalStateException("db must be prepared with db/prepare-for-indexing");
        }
        try {
            Object object3;
            IPersistentMap iPersistentMap;
            IPersistentMap m_15864 = RT.mapUniqueKeys((Object[])new Object[]{const__0, const__1, const__2, as_of_t2});
            Logger logger = LoggerFactory.getLogger((String)"datomic.index");
            if (logger.isDebugEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.debug((String)((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke((Object)m_15864, (Object)const__5, (Object)const__6)));
            }
            long start__8981__auto__15881 = System.nanoTime();
            Object object4 = cstore;
            cstore = null;
            Object object5 = db2;
            db2 = null;
            Object object6 = olookup;
            olookup = null;
            Object object7 = as_of_t2;
            as_of_t2 = null;
            Object result__8982__auto__15882 = ((IFn)new index$merge_db$fn__15867(object4, object5, object6, object7)).invoke();
            long elapsed_15865 = System.nanoTime() - start__8981__auto__15881;
            Object msec_15866 = ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_15865));
            IFn iFn = (IFn)const__9.getRawRoot();
            IPersistentMap iPersistentMap2 = m_15864;
            m_15864 = null;
            Object object8 = msec_15866;
            msec_15866 = null;
            Object object9 = ((IFn)const__4.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__10, object8, (Object)const__5, (Object)const__11);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object10 = result__8982__auto__15882;
            Object object11 = iLookupThunk.get(object10);
            if (iLookupThunk == object11) {
                __thunk__0__ = __site__0__.fault(object10);
                object11 = __thunk__0__.get(object10);
            }
            if (object11 != null && object11 != Boolean.FALSE) {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__12;
                IFn iFn2 = (IFn)const__13.getRawRoot();
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object12 = result__8982__auto__15882;
                Object object13 = iLookupThunk2.get(object12);
                if (iLookupThunk2 == object13) {
                    __thunk__1__ = __site__1__.fault(object12);
                    object13 = __thunk__1__.get(object12);
                }
                objectArray[1] = iFn2.invoke(object13);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                iPersistentMap = null;
            }
            Object endmsg__8984__auto__15879 = iFn.invoke(object9, iPersistentMap);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.index");
            if (logger3.isDebugEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                Object object14 = endmsg__8984__auto__15879;
                endmsg__8984__auto__15879 = null;
                logger4.debug((String)((IFn)const__3.getRawRoot()).invoke(object14));
            }
            Object object15 = ((IFn)const__14.getRawRoot()).invoke(result__8982__auto__15882, (Object)const__15);
            if (object15 != null && object15 != Boolean.FALSE) {
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object16 = result__8982__auto__15882;
                result__8982__auto__15882 = null;
                object3 = iLookupThunk3.get(object16);
                if (iLookupThunk3 == object3) {
                    __thunk__2__ = __site__2__.fault(object16);
                    object3 = __thunk__2__.get(object16);
                }
            } else {
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object17 = result__8982__auto__15882;
                result__8982__auto__15882 = null;
                Object object18 = iLookupThunk4.get(object17);
                if (iLookupThunk4 == object18) {
                    __thunk__3__ = __site__3__.fault(object17);
                    object18 = __thunk__3__.get(object17);
                }
                throw (Throwable)object18;
            }
            object = object3;
        }
        catch (Throwable ex3) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.index");
            Throwable ex2 = ex3;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)const__3.getRawRoot()).invoke((Object)"merge-db failed"), ex2);
                Logger logger5 = logger;
                logger = null;
                Throwable throwable = ex2;
                ex2 = null;
                ((IFn)const__16.getRawRoot()).invoke((Object)logger5, (Object)throwable);
            }
            Object ex3 = null;
            throw ex3;
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
        return index$merge_db.invokeStatic(object5, object6, object7, object8);
    }
}

