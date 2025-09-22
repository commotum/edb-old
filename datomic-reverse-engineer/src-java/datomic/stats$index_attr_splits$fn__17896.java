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

public final class stats$index_attr_splits$fn__17896
extends AFunction {
    Object a;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public stats$index_attr_splits$fn__17896(Object object) {
        this.a = object;
    }

    public Object invoke(Object p1__17890_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__17890_SHARP_;
        p1__17890_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        stats$index_attr_splits$fn__17896 this_ = null;
        return Util.equiv((Object)this_.a, (Object)object2) ? Boolean.TRUE : Boolean.FALSE;
    }
}

