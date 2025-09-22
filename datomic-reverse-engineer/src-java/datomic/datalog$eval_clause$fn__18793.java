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

public final class datalog$eval_clause$fn__18793
extends AFunction {
    Object top_bounds;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"starts"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public datalog$eval_clause$fn__18793(Object object) {
        this.top_bounds = object;
    }

    public Object invoke(Object p1__18785_SHARP_) {
        Object object;
        Object G__18794;
        Object object2;
        Object G__187942 = this_.top_bounds;
        if (Util.identical((Object)G__187942, null)) {
            object2 = null;
        } else {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = G__187942;
            G__187942 = null;
            object2 = iLookupThunk.get(object3);
            if (iLookupThunk == object2) {
                __thunk__0__ = __site__0__.fault(object3);
                object2 = G__18794 = __thunk__0__.get(object3);
            }
        }
        if (Util.identical(G__18794, null)) {
            object = null;
        } else {
            Object object4 = G__18794;
            G__18794 = null;
            Object object5 = p1__18785_SHARP_;
            p1__18785_SHARP_ = null;
            datalog$eval_clause$fn__18793 this_ = null;
            object = RT.get((Object)object4, (Object)object5);
        }
        return object;
    }
}

