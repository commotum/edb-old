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

public final class valcache$eviction_loop$evict1__9765$fn__9766
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"atime"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"atime"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public Object invoke(Object p1__9763_SHARP_, Object p2__9764_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__9763_SHARP_;
        p1__9763_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = p2__9764_SHARP_;
        p2__9764_SHARP_ = null;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        valcache$eviction_loop$evict1__9765$fn__9766 this_ = null;
        return Numbers.gt((Object)object2, (Object)object4) ? Boolean.TRUE : Boolean.FALSE;
    }
}

