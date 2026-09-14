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

public final class log$max_eidx$fn__16521
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"eid->eidx");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"e"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"tx"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public Object invoke(Object n, Object datom) {
        Object object = n;
        n = null;
        IFn.LL lL = (IFn.LL)const__1.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = datom;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = Numbers.max((Object)object, (long)lL.invokePrim(RT.longCast((Object)((Number)object3))));
        IFn.LL lL2 = (IFn.LL)const__1.getRawRoot();
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object5 = datom;
        datom = null;
        Object object6 = iLookupThunk2.get(object5);
        if (iLookupThunk2 == object6) {
            __thunk__1__ = __site__1__.fault(object5);
            object6 = __thunk__1__.get(object5);
        }
        log$max_eidx$fn__16521 this_ = null;
        return Numbers.max((Object)object4, (long)lL2.invokePrim(RT.longCast((Object)((Number)object6))));
    }
}

