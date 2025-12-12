/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class datalog$sched_in_order$pred_QMARK___18514
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"binds"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object p1__18448_SHARP_) {
        Object object;
        Object and__5236__auto__18516;
        Object object2 = and__5236__auto__18516 = ((IFn)const__0.getRawRoot()).invoke(p1__18448_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = p1__18448_SHARP_;
            p1__18448_SHARP_ = null;
            Object object4 = iLookupThunk.get(object3);
            if (iLookupThunk == object4) {
                __thunk__0__ = __site__0__.fault(object3);
                object4 = __thunk__0__.get(object3);
            }
            datalog$sched_in_order$pred_QMARK___18514 this_ = null;
            object = Util.identical((Object)object4, null) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object = and__5236__auto__18516;
            Object var2_2 = null;
        }
        return object;
    }
}

