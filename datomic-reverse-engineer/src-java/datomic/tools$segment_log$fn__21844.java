/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
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
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class tools$segment_log$fn__21844
extends AFunction {
    Object cr;
    public static final Var const__0 = RT.var((String)"datomic.log", (String)"segment");
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"cluster"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"olookup"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public tools$segment_log$fn__21844(Object object) {
        this.cr = object;
    }

    public Object invoke() {
        Boolean bl;
        try {
            IFn iFn = (IFn)const__0.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object = this.cr;
            Object object2 = iLookupThunk.get(object);
            if (iLookupThunk == object2) {
                __thunk__0__ = __site__0__.fault(object);
                object2 = __thunk__0__.get(object);
            }
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object3 = this.cr;
            Object object4 = iLookupThunk2.get(object3);
            if (iLookupThunk2 == object4) {
                __thunk__1__ = __site__1__.fault(object3);
                object4 = __thunk__1__.get(object3);
            }
            iFn.invoke(object2, object4);
            bl = Boolean.TRUE;
        }
        catch (Error e2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.tools");
            Object e2 = null;
            Error ex = e2;
            if (logger.isWarnEnabled()) {
                logger.warn((String)((IFn)const__3.getRawRoot()).invoke((Object)"Segment log failed "), (Throwable)ex);
                Logger logger2 = logger;
                logger = null;
                Error error2 = ex;
                ex = null;
                ((IFn)const__4.getRawRoot()).invoke((Object)logger2, (Object)error2);
            }
            bl = Boolean.FALSE;
        }
        return bl;
    }
}

