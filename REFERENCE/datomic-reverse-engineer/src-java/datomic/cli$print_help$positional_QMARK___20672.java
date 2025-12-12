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

public final class cli$print_help$positional_QMARK___20672
extends AFunction {
    Object pset;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"long-name"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public cli$print_help$positional_QMARK___20672(Object object) {
        this.pset = object;
    }

    public Object invoke(Object p1__20671_SHARP_) {
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__20671_SHARP_;
        p1__20671_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        cli$print_help$positional_QMARK___20672 this_ = null;
        return iFn.invoke(this_.pset, object2);
    }
}

