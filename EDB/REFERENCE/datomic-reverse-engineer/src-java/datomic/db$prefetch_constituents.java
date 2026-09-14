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
import datomic.db$prefetch_constituents$fn__13936;

public final class db$prefetch_constituents
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"run!");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"attribute");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleAttrs"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object dispatcher, Object adder, Object db2, Object e, Object a) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = e;
        e = null;
        Object object2 = adder;
        adder = null;
        Object object3 = dispatcher;
        dispatcher = null;
        db$prefetch_constituents$fn__13936 db$prefetch_constituents$fn__13936 = new db$prefetch_constituents$fn__13936(db2, object, object2, object3);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object4 = db2;
        db2 = null;
        Object object5 = a;
        a = null;
        Object object6 = ((IFn)const__2.getRawRoot()).invoke(object4, object5);
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        return iFn.invoke((Object)db$prefetch_constituents$fn__13936, object7);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return db$prefetch_constituents.invokeStatic(object6, object7, object8, object9, object10);
    }
}

