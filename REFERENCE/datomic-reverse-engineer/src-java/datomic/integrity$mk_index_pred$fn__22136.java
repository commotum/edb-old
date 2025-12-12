/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;

public final class integrity$mk_index_pred$fn__22136
extends AFunction {
    public static final Keyword const__1 = RT.keyword((String)"db.type", (String)"ref");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"value-type"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object p1__22134_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__22134_SHARP_;
        p1__22134_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        integrity$mk_index_pred$fn__22136 this_ = null;
        return Util.equiv((Object)const__1, (Object)object2) ? Boolean.TRUE : Boolean.FALSE;
    }
}

