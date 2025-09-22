/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;

public final class garbage$do_mark_garbage$fn__19798
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"vals"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object p1__19797_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__19797_SHARP_;
        p1__19797_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        garbage$do_mark_garbage$fn__19798 this_ = null;
        return RT.count((Object)object2);
    }
}

