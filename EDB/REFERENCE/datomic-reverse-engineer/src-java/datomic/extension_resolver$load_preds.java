/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class extension_resolver$load_preds
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.extension-resolver", (String)"load-extensions-config");
    public static final Keyword const__1 = RT.keyword(null, (String)"xforms");
    public static final Var const__2 = RT.var((String)"datomic.extension-resolver", (String)"allow-list->pred");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"xforms"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object rsrc) {
        Object object = rsrc;
        rsrc = null;
        Object config2 = ((IFn)const__0.getRawRoot()).invoke(object);
        Object[] objectArray = new Object[2];
        objectArray[0] = const__1;
        IFn iFn = (IFn)const__2.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = config2;
        config2 = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        objectArray[1] = iFn.invoke(object3);
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return extension_resolver$load_preds.invokeStatic(object2);
    }
}

