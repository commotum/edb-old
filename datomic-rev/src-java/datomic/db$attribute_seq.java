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

public final class db$attribute_seq
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"instance?");
    public static final Object const__3 = RT.classForName((String)"datomic.db.Attribute");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"elements"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), const__3);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = db2;
        db2 = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        return iFn.invoke(object, object3);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$attribute_seq.invokeStatic(object2);
    }
}

