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
package datomic.query;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query.support$disallow_find_variants_BANG_$fn__19075;

public final class support$disallow_find_variants_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__2 = RT.var((String)"datomic.query.support", (String)"incorrect!");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"find"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object query2) {
        IFn iFn = (IFn)const__0.getRawRoot();
        support$disallow_find_variants_BANG_$fn__19075 support$disallow_find_variants_BANG_$fn__19075 = new support$disallow_find_variants_BANG_$fn__19075();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = query2;
        query2 = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object object3 = iFn.invoke((Object)support$disallow_find_variants_BANG_$fn__19075, object2);
        return object3 != null && object3 != Boolean.FALSE ? ((IFn)const__2.getRawRoot()).invoke((Object)"Only find-rel elements are allowed in client :find") : null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return support$disallow_find_variants_BANG_.invokeStatic(object2);
    }
}

