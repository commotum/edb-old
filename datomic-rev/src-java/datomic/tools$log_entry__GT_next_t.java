/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class tools$log_entry__GT_next_t
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.log", (String)"max-eidx");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"data"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object log_entry) {
        IFn iFn = (IFn)const__1.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = log_entry;
        log_entry = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return Numbers.inc((Object)iFn.invoke(object2));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$log_entry__GT_next_t.invokeStatic(object2);
    }
}

