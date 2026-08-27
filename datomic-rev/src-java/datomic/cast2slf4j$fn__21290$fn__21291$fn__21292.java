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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class cast2slf4j$fn__21290$fn__21291$fn__21292
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"ex");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"ex"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object alarm2) {
        Object v9;
        Object temp__5455__auto__21294;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = alarm2;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object object3 = temp__5455__auto__21294 = object2;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5455__auto__21294;
            temp__5455__auto__21294 = null;
            Object ex = object4;
            Logger logger = LoggerFactory.getLogger((String)"datomic.cast2slf4j");
            Object object5 = ex;
            ex = null;
            Object ex2 = object5;
            if (logger.isWarnEnabled()) {
                Object object6 = alarm2;
                alarm2 = null;
                logger.warn((String)((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object6, (Object)const__0)), ex2);
                Logger logger2 = logger;
                logger = null;
                Object object7 = ex2;
                ex2 = null;
                ((IFn)const__3.getRawRoot()).invoke((Object)logger2, object7);
            }
            v9 = null;
        } else {
            Logger logger = LoggerFactory.getLogger((String)"datomic.cast2slf4j");
            if (logger.isWarnEnabled()) {
                Logger logger3 = logger;
                logger = null;
                Object object8 = alarm2;
                alarm2 = null;
                logger3.warn((String)((IFn)const__1.getRawRoot()).invoke(object8));
            }
            v9 = null;
        }
        return v9;
    }
}

