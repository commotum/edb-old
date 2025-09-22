/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OL
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
import datomic.clusterfs$create_file$fn__14248;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class clusterfs$create_file
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"chunk-size");
    public static final Var const__4 = RT.var((String)"datomic.common", (String)"rand-uuid");
    public static final Var const__5 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
    public static final Var const__6 = RT.var((String)"clojure.java.io", (String)"file");
    public static final Var const__7 = RT.var((String)"datomic.clusterfs", (String)"floor");
    public static final Keyword const__10 = RT.keyword(null, (String)"event");
    public static final Keyword const__11 = RT.keyword((String)"clusterfs", (String)"create-file");
    public static final Keyword const__12 = RT.keyword(null, (String)"size");
    public static final Keyword const__13 = RT.keyword(null, (String)"local-file");
    public static final Keyword const__14 = RT.keyword(null, (String)"prefix");
    public static final Keyword const__15 = RT.keyword(null, (String)"chunks");
    public static final Var const__16 = RT.var((String)"datomic.clusterfs", (String)"ceil");
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
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object cs, Object local_file, ISeq p__14243) {
        Object object;
        IPersistentMap iPersistentMap;
        ISeq map__14244;
        ISeq iSeq;
        ISeq iSeq2 = p__14243;
        p__14243 = null;
        ISeq map__142442 = iSeq2;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)map__142442);
        if (object2 != null && object2 != Boolean.FALSE) {
            ISeq iSeq3 = map__142442;
            map__142442 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__142442;
            map__142442 = null;
        }
        ISeq iSeq4 = map__14244 = iSeq;
        map__14244 = null;
        Object chunk_size = RT.get((Object)iSeq4, (Object)const__3);
        Object base = ((IFn)const__4.getRawRoot()).invoke();
        Object prefix = ((IFn)const__5.getRawRoot()).invoke(base);
        long size = ((File)((IFn)const__6.getRawRoot()).invoke(local_file)).length();
        long fullchunks = ((IFn.OL)const__7.getRawRoot()).invokePrim((Object)Numbers.divide((long)size, (Object)chunk_size));
        Number tailsize = Numbers.remainder((long)size, (Object)chunk_size);
        IPersistentMap m_14245 = RT.mapUniqueKeys((Object[])new Object[]{const__10, const__11, const__12, Numbers.num((long)size), const__13, local_file, const__14, prefix, const__15, Numbers.num((long)((IFn.OL)const__16.getRawRoot()).invokePrim((Object)Numbers.divide((long)size, (Object)chunk_size)))});
        Logger logger = LoggerFactory.getLogger((String)"datomic.clusterfs");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke((Object)m_14245, (Object)const__19, (Object)const__20)));
        }
        long start__8981__auto__14257 = System.nanoTime();
        Object object3 = base;
        base = null;
        Number number = tailsize;
        tailsize = null;
        Object object4 = local_file;
        local_file = null;
        Object object5 = prefix;
        prefix = null;
        Object object6 = chunk_size;
        chunk_size = null;
        Object object7 = cs;
        cs = null;
        Object result__8982__auto__14258 = ((IFn)new clusterfs$create_file$fn__14248(object3, number, object4, object5, object6, object7, fullchunks)).invoke();
        long elapsed_14246 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__14257);
        Object msec_14247 = ((IFn)const__22.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_14246));
        IFn iFn = (IFn)const__23.getRawRoot();
        IPersistentMap iPersistentMap2 = m_14245;
        m_14245 = null;
        Object object8 = msec_14247;
        msec_14247 = null;
        Object object9 = ((IFn)const__18.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__24, object8, (Object)const__19, (Object)const__25);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object10 = result__8982__auto__14258;
        Object object11 = iLookupThunk.get(object10);
        if (iLookupThunk == object11) {
            __thunk__0__ = __site__0__.fault(object10);
            object11 = __thunk__0__.get(object10);
        }
        if (object11 != null && object11 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__26;
            IFn iFn2 = (IFn)const__27.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object12 = result__8982__auto__14258;
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
        Object endmsg__8984__auto__14255 = iFn.invoke(object9, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.clusterfs");
        if (logger3.isInfoEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object14 = endmsg__8984__auto__14255;
            endmsg__8984__auto__14255 = null;
            logger4.info((String)((IFn)const__17.getRawRoot()).invoke(object14));
        }
        Object object15 = ((IFn)const__28.getRawRoot()).invoke(result__8982__auto__14258, (Object)const__29);
        if (object15 != null && object15 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object16 = result__8982__auto__14258;
            result__8982__auto__14258 = null;
            object = iLookupThunk3.get(object16);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object16);
                object = __thunk__2__.get(object16);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object17 = result__8982__auto__14258;
            result__8982__auto__14258 = null;
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
        return clusterfs$create_file.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

