/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic.core2.val_store.double_store;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;

public final class ValStore$fn__21186$state_machine__9975__auto____21201$fn__21203$fn__21205
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"val"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object p1__21085_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__21085_SHARP_;
        p1__21085_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        ValStore$fn__21186$state_machine__9975__auto____21201$fn__21203$fn__21205 this_ = null;
        return Util.identical((Object)object2, null) ? Boolean.TRUE : Boolean.FALSE;
    }
}

