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
import datomic.garbage$dir_seq$fn__19767;

public final class garbage$dir_seq
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapcat");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"children"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object lookup, Object root) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = lookup;
        lookup = null;
        garbage$dir_seq$fn__19767 garbage$dir_seq$fn__19767 = new garbage$dir_seq$fn__19767(object);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = root;
        root = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        return iFn.invoke((Object)garbage$dir_seq$fn__19767, object3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return garbage$dir_seq.invokeStatic(object3, object4);
    }
}

