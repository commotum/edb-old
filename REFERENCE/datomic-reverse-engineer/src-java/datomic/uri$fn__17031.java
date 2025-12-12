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

public final class uri$fn__17031
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-name"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"sql-url"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"system-root"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object cluster_conf) {
        Object object;
        Object or__5238__auto__17033;
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = cluster_conf;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object4 = cluster_conf;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        Object object6 = or__5238__auto__17033 = object5;
        if (object6 != null && object6 != Boolean.FALSE) {
            object = or__5238__auto__17033;
            or__5238__auto__17033 = null;
        } else {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object7 = cluster_conf;
            cluster_conf = null;
            object = iLookupThunk3.get(object7);
            if (iLookupThunk3 == object) {
                __thunk__2__ = __site__2__.fault(object7);
                object = __thunk__2__.get(object7);
            }
        }
        return iFn.invoke((Object)"datomic:sql://", object3, (Object)"?", object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__17031.invokeStatic(object2);
    }
}

