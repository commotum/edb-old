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

public final class stats$sizes__GT_metrics$fn__17987
extends AFunction {
    public static final Keyword const__1 = RT.keyword(null, (String)"fulltext");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"index"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object p1__17985_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__17985_SHARP_;
        p1__17985_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        stats$sizes__GT_metrics$fn__17987 this_ = null;
        return Util.equiv((Object)const__1, (Object)object2) ? Boolean.TRUE : Boolean.FALSE;
    }
}

