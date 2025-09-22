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
import datomic.Database;
import datomic.db$recalc_elements$fn__13630;

public final class db$recalc_elements
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__1 = RT.keyword(null, (String)"elements");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"mapv");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"elements"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2, Object basisT) {
        Object object = basisT;
        basisT = null;
        Database basis_db = ((Database)db2).asOf(object);
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = db2;
        IFn iFn2 = (IFn)const__2.getRawRoot();
        Database database = basis_db;
        basis_db = null;
        db$recalc_elements$fn__13630 db$recalc_elements$fn__13630 = new db$recalc_elements$fn__13630(database);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = db2;
        db2 = null;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        return iFn.invoke(object2, (Object)const__1, iFn2.invoke((Object)db$recalc_elements$fn__13630, object4));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$recalc_elements.invokeStatic(object3, object4);
    }
}

