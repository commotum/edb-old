/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;

public final class garbage$gc$fn__19866
extends AFunction {
    Object tstamp;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"start"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public garbage$gc$fn__19866(Object object) {
        this.tstamp = object;
    }

    public Object invoke(Object p1__19860_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__19860_SHARP_;
        p1__19860_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        garbage$gc$fn__19866 this_ = null;
        return Numbers.isNeg((long)Util.compare((Object)object2, (Object)this_.tstamp)) ? Boolean.TRUE : Boolean.FALSE;
    }
}

