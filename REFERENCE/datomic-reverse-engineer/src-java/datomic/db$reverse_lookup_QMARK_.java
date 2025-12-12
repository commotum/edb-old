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

public final class db$reverse_lookup_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"reverse-key?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"contains?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"_keys"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2, Object k) {
        Object object;
        Object and__5236__auto__13675;
        Object object2 = and__5236__auto__13675 = ((IFn)const__0.getRawRoot()).invoke(k);
        if (object2 != null && object2 != Boolean.FALSE) {
            IFn iFn = (IFn)const__1.getRawRoot();
            IFn iFn2 = (IFn)const__2.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = db2;
            db2 = null;
            Object object4 = iLookupThunk.get(object3);
            if (iLookupThunk == object4) {
                __thunk__0__ = __site__0__.fault(object3);
                object4 = __thunk__0__.get(object3);
            }
            Object object5 = k;
            k = null;
            object = iFn.invoke(iFn2.invoke(object4, object5));
        } else {
            object = and__5236__auto__13675;
            Object var2_2 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$reverse_lookup_QMARK_.invokeStatic(object3, object4);
    }
}

