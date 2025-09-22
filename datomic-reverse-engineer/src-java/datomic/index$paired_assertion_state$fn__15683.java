/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$paired_assertion_state$fn__15683
extends AFunction {
    Object sd1;
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"retract-assert-pair?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public index$paired_assertion_state$fn__15683(Object object) {
        this.sd1 = object;
    }

    public Object invoke(Object sdn) {
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = this_.sd1;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = sdn;
        sdn = null;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        index$paired_assertion_state$fn__15683 this_ = null;
        return iFn.invoke(object2, object4);
    }
}

