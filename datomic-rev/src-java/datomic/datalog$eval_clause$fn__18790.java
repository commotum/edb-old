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

public final class datalog$eval_clause$fn__18790
extends AFunction {
    Object top_bounds;
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"variable-or-blank?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"consts"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public datalog$eval_clause$fn__18790(Object object) {
        this.top_bounds = object;
    }

    public Object invoke(Object p1__18784_SHARP_) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(p1__18784_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object G__18791;
            Object object3;
            Object G__187912 = this_.top_bounds;
            if (Util.identical((Object)G__187912, null)) {
                object3 = null;
            } else {
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object4 = G__187912;
                G__187912 = null;
                object3 = iLookupThunk.get(object4);
                if (iLookupThunk == object3) {
                    __thunk__0__ = __site__0__.fault(object4);
                    object3 = G__18791 = __thunk__0__.get(object4);
                }
            }
            if (Util.identical(G__18791, null)) {
                object = null;
            } else {
                Object object5 = p1__18784_SHARP_;
                p1__18784_SHARP_ = null;
                Object object6 = G__18791;
                G__18791 = null;
                datalog$eval_clause$fn__18790 this_ = null;
                object = ((IFn)object5).invoke(object6);
            }
        } else {
            object = p1__18784_SHARP_;
            Object var1_1 = null;
        }
        return object;
    }
}

