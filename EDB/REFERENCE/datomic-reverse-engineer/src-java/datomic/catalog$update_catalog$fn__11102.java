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

public final class catalog$update_catalog$fn__11102
extends AFunction {
    public static final Keyword const__1 = RT.keyword(null, (String)"conflict");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"failed"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object p1__11096__11097__auto__) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__11096__11097__auto__;
        p1__11096__11097__auto__ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        catalog$update_catalog$fn__11102 this_ = null;
        return Util.equiv((Object)const__1, (Object)object2) ? Boolean.TRUE : Boolean.FALSE;
    }
}

