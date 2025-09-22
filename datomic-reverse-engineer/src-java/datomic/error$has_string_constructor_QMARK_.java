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
import datomic.error$has_string_constructor_QMARK_$fn__688;
import datomic.error$has_string_constructor_QMARK_$fn__690;

public final class error$has_string_constructor_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__4 = RT.var((String)"clojure.reflect", (String)"reflect");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"members"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cls) {
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        IFn iFn3 = (IFn)const__2.getRawRoot();
        error$has_string_constructor_QMARK_$fn__688 error$has_string_constructor_QMARK_$fn__688 = new error$has_string_constructor_QMARK_$fn__688();
        IFn iFn4 = (IFn)const__2.getRawRoot();
        error$has_string_constructor_QMARK_$fn__690 error$has_string_constructor_QMARK_$fn__690 = new error$has_string_constructor_QMARK_$fn__690();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = cls;
        cls = null;
        Object object2 = ((IFn)const__4.getRawRoot()).invoke(object);
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        return iFn.invoke(iFn2.invoke(iFn3.invoke((Object)error$has_string_constructor_QMARK_$fn__688, iFn4.invoke((Object)error$has_string_constructor_QMARK_$fn__690, object3))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return error$has_string_constructor_QMARK_.invokeStatic(object2);
    }
}

