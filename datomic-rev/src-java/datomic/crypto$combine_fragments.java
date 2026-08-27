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
import javax.crypto.spec.SecretKeySpec;

public final class crypto$combine_fragments
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.codec", (String)"decode-64");
    public static final Var const__1 = RT.var((String)"datomic.codec", (String)"string->bytes");
    public static final Var const__3 = RT.var((String)"datomic.crypto", (String)"xor-arrays");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"fragment"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"fragment"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"algorithm"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object f1, Object f2) {
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = f1;
        f1 = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object b1 = iFn.invoke(iFn2.invoke(object2));
        IFn iFn3 = (IFn)const__0.getRawRoot();
        IFn iFn4 = (IFn)const__1.getRawRoot();
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = f2;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        Object b2 = iFn3.invoke(iFn4.invoke(object4));
        Object object5 = b1;
        b1 = null;
        Object object6 = b2;
        b2 = null;
        byte[] byArray = (byte[])((IFn)const__3.getRawRoot()).invoke(object5, object6);
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object7 = f2;
        f2 = null;
        Object object8 = iLookupThunk3.get(object7);
        if (iLookupThunk3 == object8) {
            __thunk__2__ = __site__2__.fault(object7);
            object8 = __thunk__2__.get(object7);
        }
        return new SecretKeySpec(byArray, (String)object8);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return crypto$combine_fragments.invokeStatic(object3, object4);
    }
}

