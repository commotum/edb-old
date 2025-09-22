/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;

public final class error$has_string_constructor_QMARK_$fn__688
extends AFunction {
    public static final AFn const__1 = (AFn)Tuple.create((Object)Symbol.intern(null, (String)"java.lang.String"));
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"parameter-types"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object p1__687_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__687_SHARP_;
        p1__687_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        error$has_string_constructor_QMARK_$fn__688 this_ = null;
        return Util.equiv((Object)const__1, (Object)object2) ? Boolean.TRUE : Boolean.FALSE;
    }
}

