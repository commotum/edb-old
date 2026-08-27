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

public final class tools$unique_values$fn__21828
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.api", (String)"attribute");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"v"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public tools$unique_values$fn__21828(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__21824_SHARP_) {
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__21824_SHARP_;
        p1__21824_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        tools$unique_values$fn__21828 this_ = null;
        return iFn.invoke(this_.db, object2);
    }
}

