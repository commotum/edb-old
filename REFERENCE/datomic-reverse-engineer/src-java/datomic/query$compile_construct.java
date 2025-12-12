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

public final class query$compile_construct
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.query", (String)"compile-construct-n");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__4 = RT.keyword(null, (String)"construct-fn-src");
    public static final Keyword const__5 = RT.keyword(null, (String)"construct-fn");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"eval");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"construct"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"find"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object query2) {
        Object object;
        Object temp__5455__auto__19425;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = query2;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = temp__5455__auto__19425 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5455__auto__19425;
            temp__5455__auto__19425 = null;
            Object construct2 = object5;
            IFn iFn = (IFn)const__1.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object6 = query2;
            Object object7 = iLookupThunk2.get(object6);
            if (iLookupThunk2 == object7) {
                __thunk__1__ = __site__1__.fault(object6);
                object7 = __thunk__1__.get(object6);
            }
            Object object8 = construct2;
            construct2 = null;
            Object src = iFn.invoke(object7, object8);
            Object object9 = query2;
            query2 = null;
            Object object10 = src;
            Object object11 = src;
            src = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object9, (Object)const__4, object10, (Object)const__5, ((IFn)const__6.getRawRoot()).invoke(object11));
        } else {
            object = query2;
            Object object12 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$compile_construct.invokeStatic(object2);
    }
}

