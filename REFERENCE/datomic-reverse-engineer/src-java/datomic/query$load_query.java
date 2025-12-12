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

public final class query$load_query
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.query", (String)"parse-query");
    public static final Var const__1 = RT.var((String)"datomic.query", (String)"move-sources-to-meta");
    public static final Var const__2 = RT.var((String)"datomic.datalog", (String)"prep-clauses");
    public static final Keyword const__3 = RT.keyword(null, (String)"where");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__8 = RT.keyword(null, (String)"arules");
    public static final Var const__9 = RT.var((String)"datomic.query", (String)"compile-construct");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"where"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object query2) {
        Object query3;
        Object query4;
        Object query5;
        Object object = query2;
        query2 = null;
        Object object2 = query5 = ((IFn)const__0.getRawRoot()).invoke(object);
        query5 = null;
        Object query6 = ((IFn)const__1.getRawRoot()).invoke(object2);
        IFn iFn = (IFn)const__2.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = query6;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        Object vec__19445 = iFn.invoke(null, object4);
        Object rm = RT.nth((Object)vec__19445, (int)RT.intCast((long)0L), null);
        Object object5 = vec__19445;
        vec__19445 = null;
        Object cs = RT.nth((Object)object5, (int)RT.intCast((long)1L), null);
        Object object6 = query6;
        query6 = null;
        Object object7 = cs;
        cs = null;
        Object object8 = rm;
        rm = null;
        Object object9 = query4 = ((IFn)const__7.getRawRoot()).invoke(object6, (Object)const__3, object7, (Object)const__8, object8);
        query4 = null;
        Object object10 = query3 = ((IFn)const__9.getRawRoot()).invoke(object9);
        query3 = null;
        return object10;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$load_query.invokeStatic(object2);
    }
}

