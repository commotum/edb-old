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

public final class treewalk$fn__19708
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"vals");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"attrmap"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object root) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = root;
        root = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        return iFn.invoke(object, iFn2.invoke(object3));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return treewalk$fn__19708.invokeStatic(object2);
    }
}

