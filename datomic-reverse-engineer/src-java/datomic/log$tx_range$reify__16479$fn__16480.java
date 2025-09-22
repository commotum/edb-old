/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;

public final class log$tx_range$reify__16479$fn__16480
extends AFunction {
    Object end;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public log$tx_range$reify__16479$fn__16480(Object object) {
        this.end = object;
    }

    public Object invoke(Object p1__16478_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__16478_SHARP_;
        p1__16478_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        log$tx_range$reify__16479$fn__16480 this_ = null;
        return Numbers.lt((Object)object2, (Object)this_.end) ? Boolean.TRUE : Boolean.FALSE;
    }
}

