/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class log$write_tail_descriptor
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__1 = RT.keyword(null, (String)"event");
    public static final Keyword const__2 = RT.keyword((String)"log", (String)"write-tail");
    public static final Keyword const__3 = RT.keyword(null, (String)"tail-desc");
    public static final Var const__4 = RT.var((String)"datomic.log", (String)"tail-descriptor?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__7 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"tail-descriptor?"), Symbol.intern(null, (String)"desc")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Keyword const__8 = RT.keyword((String)"d", (String)"r");
    public static final Keyword const__9 = RT.keyword((String)"d", (String)"v");
    public static final Var const__10 = RT.var((String)"datomic.config", (String)"property");
    public static final Keyword const__11 = RT.keyword((String)"d", (String)"l");
    public static final Object const__12 = 3L;
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__14 = RT.var((String)"datomic.cluster", (String)"update-pod");
    public static final Var const__15 = RT.var((String)"datomic.log", (String)"tail-pod-key");
    public static final Var const__18 = RT.var((String)"datomic.log", (String)"pod-update-succeeded?");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__21 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"rev"), (Object)RT.keyword(null, (String)"etag"));
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"d", (String)"r"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"etag"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object cs, Object desc, Object buf) {
        Object object;
        Logger logger = LoggerFactory.getLogger((String)"datomic.log");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, const__2, const__3, desc})));
        }
        Object object2 = ((IFn)const__4.getRawRoot()).invoke(desc);
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__5.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__6.getRawRoot()).invoke(const__7))));
        }
        Object[] objectArray = new Object[6];
        objectArray[0] = const__8;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = desc;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        objectArray[1] = object4;
        objectArray[2] = const__9;
        objectArray[3] = ((IFn)const__10.getRawRoot()).invoke((Object)"datomic.versionUnique");
        objectArray[4] = const__11;
        objectArray[5] = const__12;
        IPersistentMap pod_meta = RT.mapUniqueKeys((Object[])objectArray);
        IFn iFn = (IFn)const__13.getRawRoot();
        IFn iFn2 = (IFn)const__14.getRawRoot();
        Object object5 = cs;
        Object object6 = cs;
        cs = null;
        Object object7 = ((IFn)const__15.getRawRoot()).invoke(object6);
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object8 = desc;
        Object object9 = iLookupThunk2.get(object8);
        if (iLookupThunk2 == object9) {
            __thunk__1__ = __site__1__.fault(object8);
            object9 = __thunk__1__.get(object8);
        }
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object10 = desc;
        desc = null;
        Object object11 = iLookupThunk3.get(object10);
        if (iLookupThunk3 == object11) {
            __thunk__2__ = __site__2__.fault(object10);
            object11 = __thunk__2__.get(object10);
        }
        Object object12 = buf;
        buf = null;
        Object result2 = iFn.invoke(iFn2.invoke(object5, object7, object9, object11, object12, (Object)pod_meta));
        Object object13 = ((IFn)const__18.getRawRoot()).invoke(result2);
        if (object13 != null && object13 != Boolean.FALSE) {
            IPersistentMap iPersistentMap = pod_meta;
            pod_meta = null;
            Object object14 = result2;
            result2 = null;
            object = ((IFn)const__19.getRawRoot()).invoke((Object)iPersistentMap, ((IFn)const__20.getRawRoot()).invoke(object14, (Object)const__21));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return log$write_tail_descriptor.invokeStatic(object4, object5, object6);
    }
}

