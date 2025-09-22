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

public final class stats$index_attr_splits$fn__17907
extends AFunction {
    Object a;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public stats$index_attr_splits$fn__17907(Object object) {
        this.a = object;
    }

    public Object invoke(Object p1__17892_SHARP_) {
        Object object;
        ILookupThunk iLookupThunk = __thunk__1__;
        ILookupThunk iLookupThunk2 = __thunk__0__;
        Object object2 = p1__17892_SHARP_;
        p1__17892_SHARP_ = null;
        Object object3 = iLookupThunk2.get(object2);
        if (iLookupThunk2 == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (iLookupThunk == (object = iLookupThunk.get(object3))) {
            __thunk__1__ = __site__1__.fault(object3);
            object = __thunk__1__.get(object3);
        }
        stats$index_attr_splits$fn__17907 this_ = null;
        return Util.equiv((Object)this_.a, (Object)object) ? Boolean.TRUE : Boolean.FALSE;
    }
}

