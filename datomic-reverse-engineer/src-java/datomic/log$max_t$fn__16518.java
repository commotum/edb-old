/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LL
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

public final class log$max_t$fn__16518
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"eid->eidx");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tx"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object n, Object datom) {
        Object object = n;
        n = null;
        IFn.LL lL = (IFn.LL)const__1.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = datom;
        datom = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        log$max_t$fn__16518 this_ = null;
        return Numbers.max((Object)object, (long)lL.invokePrim(RT.longCast((Object)((Number)object3))));
    }
}

