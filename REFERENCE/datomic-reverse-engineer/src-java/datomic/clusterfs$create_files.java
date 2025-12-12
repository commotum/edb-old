/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.clusterfs$create_files$fn__14264;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class clusterfs$create_files
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"chunk-size");
    public static final Keyword const__4 = RT.keyword(null, (String)"event");
    public static final Keyword const__5 = RT.keyword((String)"clusterfs", (String)"create-files");
    public static final Keyword const__6 = RT.keyword(null, (String)"count");
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__10 = RT.keyword(null, (String)"phase");
    public static final Keyword const__11 = RT.keyword(null, (String)"begin");
    public static final Var const__13 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__15 = RT.keyword(null, (String)"msec");
    public static final Keyword const__16 = RT.keyword(null, (String)"end");
    public static final Keyword const__17 = RT.keyword(null, (String)"threw");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__20 = RT.keyword(null, (String)"returned");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object cs, Object files2, ISeq p__14259) {
        Object object;
        IPersistentMap iPersistentMap;
        ISeq map__14260;
        ISeq iSeq;
        ISeq iSeq2 = p__14259;
        p__14259 = null;
        ISeq map__142602 = iSeq2;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)map__142602);
        if (object2 != null && object2 != Boolean.FALSE) {
            ISeq iSeq3 = map__142602;
            map__142602 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__142602;
            map__142602 = null;
        }
        ISeq iSeq4 = map__14260 = iSeq;
        map__14260 = null;
        Object chunk_size = RT.get((Object)iSeq4, (Object)const__3);
        IPersistentMap m_14261 = RT.mapUniqueKeys((Object[])new Object[]{const__4, const__5, const__6, RT.count((Object)files2)});
        Logger logger = LoggerFactory.getLogger((String)"datomic.clusterfs");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)m_14261, (Object)const__10, (Object)const__11)));
        }
        long start__8981__auto__14276 = System.nanoTime();
        Object object3 = cs;
        cs = null;
        Object object4 = files2;
        files2 = null;
        Object object5 = chunk_size;
        chunk_size = null;
        Object result__8982__auto__14277 = ((IFn)new clusterfs$create_files$fn__14264(object3, object4, object5)).invoke();
        long elapsed_14262 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__14276);
        Object msec_14263 = ((IFn)const__13.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_14262));
        IFn iFn = (IFn)const__14.getRawRoot();
        IPersistentMap iPersistentMap2 = m_14261;
        m_14261 = null;
        Object object6 = msec_14263;
        msec_14263 = null;
        Object object7 = ((IFn)const__9.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__15, object6, (Object)const__10, (Object)const__16);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object8 = result__8982__auto__14277;
        Object object9 = iLookupThunk.get(object8);
        if (iLookupThunk == object9) {
            __thunk__0__ = __site__0__.fault(object8);
            object9 = __thunk__0__.get(object8);
        }
        if (object9 != null && object9 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__17;
            IFn iFn2 = (IFn)const__18.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object10 = result__8982__auto__14277;
            Object object11 = iLookupThunk2.get(object10);
            if (iLookupThunk2 == object11) {
                __thunk__1__ = __site__1__.fault(object10);
                object11 = __thunk__1__.get(object10);
            }
            objectArray[1] = iFn2.invoke(object11);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__14274 = iFn.invoke(object7, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.clusterfs");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object12 = endmsg__8984__auto__14274;
            endmsg__8984__auto__14274 = null;
            logger4.debug((String)((IFn)const__8.getRawRoot()).invoke(object12));
        }
        Object object13 = ((IFn)const__19.getRawRoot()).invoke(result__8982__auto__14277, (Object)const__20);
        if (object13 != null && object13 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object14 = result__8982__auto__14277;
            result__8982__auto__14277 = null;
            object = iLookupThunk3.get(object14);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object14);
                object = __thunk__2__.get(object14);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object15 = result__8982__auto__14277;
            result__8982__auto__14277 = null;
            Object object16 = iLookupThunk4.get(object15);
            if (iLookupThunk4 == object16) {
                __thunk__3__ = __site__3__.fault(object15);
                object16 = __thunk__3__.get(object15);
            }
            throw (Throwable)object16;
        }
        return object;
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return clusterfs$create_files.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

