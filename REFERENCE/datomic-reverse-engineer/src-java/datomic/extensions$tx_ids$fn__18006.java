/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LLL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class extensions$tx_ids$fn__18006
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"make-eid");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object p1__18005_SHARP_) {
        IFn.LLL lLL = (IFn.LLL)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__18005_SHARP_;
        p1__18005_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return Numbers.num((long)lLL.invokePrim(3L, RT.longCast((Object)((Number)object2))));
    }
}

