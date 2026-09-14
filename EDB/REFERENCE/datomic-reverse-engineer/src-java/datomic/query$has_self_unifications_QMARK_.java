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
import datomic.query$has_self_unifications_QMARK_$fn__19416;

public final class query$has_self_unifications_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"remove");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"where"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object qmap) {
        IFn iFn = (IFn)const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        query$has_self_unifications_QMARK_$fn__19416 query$has_self_unifications_QMARK_$fn__19416 = new query$has_self_unifications_QMARK_$fn__19416();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = qmap;
        qmap = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return RT.booleanCast((Object)iFn.invoke(iFn2.invoke((Object)query$has_self_unifications_QMARK_$fn__19416, object2))) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$has_self_unifications_QMARK_.invokeStatic(object2);
    }
}

