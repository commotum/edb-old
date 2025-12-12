/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
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
import datomic.query$sort_for_pull$fn__19512;

public final class query$sort_for_pull
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keep-indexed");
    public static final Var const__3 = RT.var((String)"datomic.query", (String)"sort-collection-by-indexed");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"pull"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object query2, Object result2) {
        Object object;
        Object temp__5455__auto__19515;
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        query$sort_for_pull$fn__19512 query$sort_for_pull$fn__19512 = new query$sort_for_pull$fn__19512();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = query2;
        query2 = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = temp__5455__auto__19515 = iFn.invoke(iFn2.invoke((Object)query$sort_for_pull$fn__19512, object3));
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5455__auto__19515;
            temp__5455__auto__19515 = null;
            Object idx = object5;
            Object object6 = result2;
            result2 = null;
            Object object7 = idx;
            idx = null;
            object = ((IFn.OLO)const__3.getRawRoot()).invokePrim(object6, RT.longCast((Object)((Number)object7)));
        } else {
            object = result2;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$sort_for_pull.invokeStatic(object3, object4);
    }
}

