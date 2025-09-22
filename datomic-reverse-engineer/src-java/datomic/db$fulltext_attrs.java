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

public final class db$fulltext_attrs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"fulltext?");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"get-entity");
    public static final Object const__5 = 0L;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"db.install", (String)"attribute"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), db2);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = db2;
        db2 = null;
        Object object3 = ((IFn)const__4.getRawRoot()).invoke(object2, const__5);
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        return iFn.invoke(object, object4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$fulltext_attrs.invokeStatic(object2);
    }
}

