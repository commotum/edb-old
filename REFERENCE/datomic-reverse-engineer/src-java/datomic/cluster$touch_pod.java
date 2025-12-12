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

public final class cluster$touch_pod
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.cluster", (String)"update-pod");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"etag"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object cs, Object pod_key, Object pod2) {
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        Object object = cs;
        cs = null;
        Object object2 = pod_key;
        pod_key = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = pod2;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        Number number = Numbers.inc((Object)object4);
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object5 = pod2;
        pod2 = null;
        Object object6 = iLookupThunk2.get(object5);
        if (iLookupThunk2 == object6) {
            __thunk__1__ = __site__1__.fault(object5);
            object6 = __thunk__1__.get(object5);
        }
        return iFn.invoke(iFn2.invoke(object, object2, (Object)number, object6, null));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cluster$touch_pod.invokeStatic(object4, object5, object6);
    }
}

