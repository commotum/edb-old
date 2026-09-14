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
import datomic.integrity$fulltext_path_reachability$fn__22418;

public final class integrity$fulltext_path_reachability
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapcat");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"attrmap"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object ft, Object db2, Object olookup, Object progress) {
        Object object;
        Object object2 = ft;
        if (object2 != null && object2 != Boolean.FALSE) {
            IFn iFn = (IFn)const__0.getRawRoot();
            Object object3 = olookup;
            olookup = null;
            Object object4 = db2;
            db2 = null;
            Object object5 = progress;
            progress = null;
            integrity$fulltext_path_reachability$fn__22418 integrity$fulltext_path_reachability$fn__22418 = new integrity$fulltext_path_reachability$fn__22418(object3, object4, object5);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object6 = ft;
            ft = null;
            Object object7 = iLookupThunk.get(object6);
            if (iLookupThunk == object7) {
                __thunk__0__ = __site__0__.fault(object6);
                object7 = __thunk__0__.get(object6);
            }
            object = iFn.invoke((Object)integrity$fulltext_path_reachability$fn__22418, object7);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return integrity$fulltext_path_reachability.invokeStatic(object5, object6, object7, object8);
    }
}

