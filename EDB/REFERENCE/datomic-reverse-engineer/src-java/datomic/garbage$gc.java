/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.garbage$gc$fn__19862;
import datomic.garbage$gc$fn__19866;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class garbage$gc
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.garbage", (String)"gc");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__2 = RT.var((String)"datomic.garbage", (String)"gc-get-node");
    public static final Var const__3 = RT.var((String)"datomic.cluster", (String)"val-key->uuid");
    public static final Var const__5 = RT.var((String)"datomic.garbage", (String)"ensure-root-ref");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Object const__7 = 0L;
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"take-while");
    public static final Var const__10 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__11 = RT.keyword(null, (String)"event");
    public static final Keyword const__12 = RT.keyword((String)"garbage", (String)"collected");
    public static final Keyword const__13 = RT.keyword(null, (String)"count");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"children"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object cluster2, Object tstamp, Object progress) {
        IFn iFn = (IFn)const__2.getRawRoot();
        IFn iFn2 = (IFn)const__3.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = ((IFn)const__5.getRawRoot()).invoke(cluster2);
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object root = iFn.invoke(cluster2, iFn2.invoke(object2));
        IFn iFn3 = (IFn)const__6.getRawRoot();
        Object object3 = cluster2;
        cluster2 = null;
        Object object4 = progress;
        progress = null;
        garbage$gc$fn__19862 garbage$gc$fn__19862 = new garbage$gc$fn__19862(tstamp, object3, object4);
        IFn iFn4 = (IFn)const__8.getRawRoot();
        Object object5 = tstamp;
        tstamp = null;
        garbage$gc$fn__19866 garbage$gc$fn__19866 = new garbage$gc$fn__19866(object5);
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object6 = root;
        root = null;
        Object object7 = iLookupThunk2.get(object6);
        if (iLookupThunk2 == object7) {
            __thunk__1__ = __site__1__.fault(object6);
            object7 = __thunk__1__.get(object6);
        }
        Object count2 = iFn3.invoke((Object)garbage$gc$fn__19862, const__7, iFn4.invoke((Object)garbage$gc$fn__19866, object7));
        Logger logger = LoggerFactory.getLogger((String)"datomic.garbage");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__10.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__11, const__12, const__13, count2})));
        }
        Object object8 = count2;
        count2 = null;
        return object8;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return garbage$gc.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object cluster2, Object tstamp) {
        Object object = cluster2;
        cluster2 = null;
        Object object2 = tstamp;
        tstamp = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, const__1.getRawRoot());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return garbage$gc.invokeStatic(object3, object4);
    }
}

