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

public final class coordination$allowed_valcache_client_QMARK_$fn__11707
extends AFunction {
    Object client;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"allowed-clients"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public coordination$allowed_valcache_client_QMARK_$fn__11707(Object object) {
        this.client = object;
    }

    public Object invoke(Object p1__11706_SHARP_) {
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__11706_SHARP_;
        p1__11706_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        coordination$allowed_valcache_client_QMARK_$fn__11707 this_ = null;
        return iFn.invoke(object2, this_.client);
    }
}

