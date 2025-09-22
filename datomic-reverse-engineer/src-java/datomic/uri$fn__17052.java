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

public final class uri$fn__17052
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object temp__5457__auto__17054;
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = cluster_conf;
        cluster_conf = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = temp__5457__auto__17054 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object db_name;
            Object object5 = temp__5457__auto__17054;
            temp__5457__auto__17054 = null;
            Object object6 = db_name = object5;
            db_name = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object6);
        } else {
            object = null;
        }
        return iFn.invoke((Object)"datomic:mem://", object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__17052.invokeStatic(object2);
    }
}

