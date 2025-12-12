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
import datomic.clusterfs$create_fs$fn__14287;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class clusterfs$create_fs
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"chunk-size");
    public static final Keyword const__4 = RT.keyword(null, (String)"base");
    public static final Var const__5 = RT.var((String)"datomic.common", (String)"rand-uuid");
    public static final Keyword const__6 = RT.keyword(null, (String)"event");
    public static final Keyword const__7 = RT.keyword((String)"clusterfs", (String)"create-fs");
    public static final Keyword const__8 = RT.keyword(null, (String)"uuid");
    public static final Var const__9 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__11 = RT.keyword(null, (String)"phase");
    public static final Keyword const__12 = RT.keyword(null, (String)"begin");
    public static final Var const__14 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__15 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__16 = RT.keyword(null, (String)"StorageCreateFSMsec");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__18 = RT.keyword(null, (String)"msec");
    public static final Keyword const__19 = RT.keyword(null, (String)"end");
    public static final Keyword const__20 = RT.keyword(null, (String)"threw");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__23 = RT.keyword(null, (String)"returned");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object cs, Object files2, ISeq p__14282) {
        Object object;
        IPersistentMap iPersistentMap;
        ISeq iSeq;
        ISeq iSeq2 = p__14282;
        p__14282 = null;
        ISeq map__14283 = iSeq2;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)map__14283);
        if (object2 != null && object2 != Boolean.FALSE) {
            ISeq iSeq3 = map__14283;
            map__14283 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__14283;
            map__14283 = null;
        }
        ISeq map__142832 = iSeq;
        Object chunk_size = RT.get((Object)map__142832, (Object)const__3);
        ISeq iSeq4 = map__142832;
        map__142832 = null;
        Object base = RT.get((Object)iSeq4, (Object)const__4);
        Object dirid = ((IFn)const__5.getRawRoot()).invoke();
        IPersistentMap m_14284 = RT.mapUniqueKeys((Object[])new Object[]{const__6, const__7, const__8, dirid});
        Logger logger = LoggerFactory.getLogger((String)"datomic.clusterfs");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke((Object)m_14284, (Object)const__11, (Object)const__12)));
        }
        long start__8981__auto__14292 = System.nanoTime();
        Object object3 = chunk_size;
        chunk_size = null;
        Object object4 = base;
        base = null;
        Object object5 = cs;
        cs = null;
        Object object6 = dirid;
        dirid = null;
        Object object7 = files2;
        files2 = null;
        Object result__8982__auto__14293 = ((IFn)new clusterfs$create_fs$fn__14287(object3, object4, object5, object6, object7)).invoke();
        long elapsed_14285 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__14292);
        Object msec_14286 = ((IFn)const__14.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_14285));
        ((IFn)const__15.getRawRoot()).invoke((Object)const__16, msec_14286);
        IFn iFn = (IFn)const__17.getRawRoot();
        IPersistentMap iPersistentMap2 = m_14284;
        m_14284 = null;
        Object object8 = msec_14286;
        msec_14286 = null;
        Object object9 = ((IFn)const__10.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__18, object8, (Object)const__11, (Object)const__19);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object10 = result__8982__auto__14293;
        Object object11 = iLookupThunk.get(object10);
        if (iLookupThunk == object11) {
            __thunk__0__ = __site__0__.fault(object10);
            object11 = __thunk__0__.get(object10);
        }
        if (object11 != null && object11 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__20;
            IFn iFn2 = (IFn)const__21.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object12 = result__8982__auto__14293;
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
        Object endmsg__8984__auto__14290 = iFn.invoke(object9, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.clusterfs");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object14 = endmsg__8984__auto__14290;
            endmsg__8984__auto__14290 = null;
            logger4.debug((String)((IFn)const__9.getRawRoot()).invoke(object14));
        }
        Object object15 = ((IFn)const__22.getRawRoot()).invoke(result__8982__auto__14293, (Object)const__23);
        if (object15 != null && object15 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object16 = result__8982__auto__14293;
            result__8982__auto__14293 = null;
            object = iLookupThunk3.get(object16);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object16);
                object = __thunk__2__.get(object16);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object17 = result__8982__auto__14293;
            result__8982__auto__14293 = null;
            Object object18 = iLookupThunk4.get(object17);
            if (iLookupThunk4 == object18) {
                __thunk__3__ = __site__3__.fault(object17);
                object18 = __thunk__3__.get(object17);
            }
            throw (Throwable)object18;
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
        return clusterfs$create_fs.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

