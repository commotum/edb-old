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

public final class index$merge_one_index$f__15304__auto____15473$fn__15495
extends AFunction {
    Object des;
    Object lt;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"fnext");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public index$merge_one_index$f__15304__auto____15473$fn__15495(Object object, Object object2) {
        this.des = object;
        this.lt = object2;
    }

    public Object invoke(Object p1__15445_SHARP_) {
        IFn iFn = (IFn)this_.lt;
        Object object = p1__15445_SHARP_;
        p1__15445_SHARP_ = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = ((IFn)const__1.getRawRoot()).invoke(this_.des);
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        index$merge_one_index$f__15304__auto____15473$fn__15495 this_ = null;
        return iFn.invoke(object, object3);
    }
}

