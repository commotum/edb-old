/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class datalog$sched_in_order$src__18470
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"meta");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)"$");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tag"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object p1__18443_SHARP_) {
        Object object;
        Object or__5238__auto__18472;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = p1__18443_SHARP_;
        p1__18443_SHARP_ = null;
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(object2);
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        Object object5 = or__5238__auto__18472 = object4;
        if (object5 != null && object5 != Boolean.FALSE) {
            object = or__5238__auto__18472;
            or__5238__auto__18472 = null;
        } else {
            object = const__2;
        }
        return object;
    }
}

