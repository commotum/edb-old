/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.tools$replace_index$fn__21755;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class tools$replace_index
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"string?"), Symbol.intern(null, (String)"index-id")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 10}));
    public static final Keyword const__4 = RT.keyword(null, (String)"event");
    public static final Keyword const__5 = RT.keyword((String)"tools", (String)"replace-index");
    public static final Keyword const__6 = RT.keyword(null, (String)"id");
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

    public static Object invokeStatic(Object cluster2, Object index_id) {
        Object object;
        IPersistentMap iPersistentMap;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(index_id);
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__3))));
        }
        IPersistentMap m_21752 = RT.mapUniqueKeys((Object[])new Object[]{const__4, const__5, const__6, index_id});
        Logger logger = LoggerFactory.getLogger((String)"datomic.tools");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke((Object)m_21752, (Object)const__9, (Object)const__10)));
        }
        long start__8981__auto__21762 = System.nanoTime();
        Object object3 = index_id;
        index_id = null;
        Object object4 = cluster2;
        cluster2 = null;
        Object result__8982__auto__21763 = ((IFn)new tools$replace_index$fn__21755(object3, object4)).invoke();
        long elapsed_21753 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__21762);
        Object msec_21754 = ((IFn)const__12.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_21753));
        IFn iFn = (IFn)const__13.getRawRoot();
        IPersistentMap iPersistentMap2 = m_21752;
        m_21752 = null;
        Object object5 = msec_21754;
        msec_21754 = null;
        Object object6 = ((IFn)const__8.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__14, object5, (Object)const__9, (Object)const__15);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = result__8982__auto__21763;
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        if (object8 != null && object8 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__16;
            IFn iFn2 = (IFn)const__17.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object9 = result__8982__auto__21763;
            Object object10 = iLookupThunk2.get(object9);
            if (iLookupThunk2 == object10) {
                __thunk__1__ = __site__1__.fault(object9);
                object10 = __thunk__1__.get(object9);
            }
            objectArray[1] = iFn2.invoke(object10);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__21760 = iFn.invoke(object6, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.tools");
        if (logger3.isInfoEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object11 = endmsg__8984__auto__21760;
            endmsg__8984__auto__21760 = null;
            logger4.info((String)((IFn)const__7.getRawRoot()).invoke(object11));
        }
        Object object12 = ((IFn)const__18.getRawRoot()).invoke(result__8982__auto__21763, (Object)const__19);
        if (object12 != null && object12 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object13 = result__8982__auto__21763;
            result__8982__auto__21763 = null;
            object = iLookupThunk3.get(object13);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object13);
                object = __thunk__2__.get(object13);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object14 = result__8982__auto__21763;
            result__8982__auto__21763 = null;
            Object object15 = iLookupThunk4.get(object14);
            if (iLookupThunk4 == object15) {
                __thunk__3__ = __site__3__.fault(object14);
                object15 = __thunk__3__.get(object14);
            }
            throw (Throwable)object15;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return tools$replace_index.invokeStatic(object3, object4);
    }
}

