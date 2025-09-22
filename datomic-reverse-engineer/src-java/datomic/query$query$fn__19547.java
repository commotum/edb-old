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

public final class query$query$fn__19547
extends AFunction {
    Object f;
    Object query_map;
    public static final Var const__0 = RT.var((String)"datomic.measure.query-stats", (String)"with-query-stats");
    public static final Keyword const__1 = RT.keyword(null, (String)"query");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"query"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public query$query$fn__19547(Object object, Object object2) {
        this.f = object;
        this.query_map = object2;
    }

    public Object invoke() {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object[] objectArray = new Object[2];
        objectArray[0] = const__1;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = this_.query_map;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        objectArray[1] = object2;
        query$query$fn__19547 this_ = null;
        return iFn.invoke(this_.f, (Object)RT.mapUniqueKeys((Object[])objectArray));
    }
}

