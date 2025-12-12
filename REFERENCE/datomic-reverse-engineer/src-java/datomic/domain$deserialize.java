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

public final class domain$deserialize
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.domain", (String)"defressian");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"buf"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object m) {
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = m;
        m = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return iFn.invoke(object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return domain$deserialize.invokeStatic(object2);
    }
}

