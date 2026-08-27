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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;

public final class index$excise_ents$fn__15517
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object p1__15513_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__15513_SHARP_;
        p1__15513_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        index$excise_ents$fn__15517 this_ = null;
        return Util.equiv((Object)object2, (long)15L) ? Boolean.TRUE : Boolean.FALSE;
    }
}

