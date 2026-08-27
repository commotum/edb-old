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

public final class integrity$crosscheck_log$fn__22147$fn__22152
extends AFunction {
    Object limit_tx;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tx"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public integrity$crosscheck_log$fn__22147$fn__22152(Object object) {
        this.limit_tx = object;
    }

    public Object invoke(Object p1__22141_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__22141_SHARP_;
        p1__22141_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        integrity$crosscheck_log$fn__22147$fn__22152 this_ = null;
        return Numbers.lte((Object)object2, (Object)this_.limit_tx) ? Boolean.TRUE : Boolean.FALSE;
    }
}

