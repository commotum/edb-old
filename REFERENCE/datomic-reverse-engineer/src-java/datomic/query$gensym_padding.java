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

public final class query$gensym_padding
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"repeat");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"max");
    public static final Object const__4 = 0L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"dec");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"count");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__10 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"flatten");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"where"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object qmap) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        IFn iFn3 = (IFn)const__0.getRawRoot();
        Object object2 = const__3.getRawRoot();
        IFn iFn4 = (IFn)const__5.getRawRoot();
        Object object3 = ((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), const__8.getRawRoot(), const__1.getRawRoot());
        IFn iFn5 = (IFn)const__9.getRawRoot();
        Object object4 = const__10.getRawRoot();
        IFn iFn6 = (IFn)const__11.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object5 = qmap;
        qmap = null;
        Object object6 = iLookupThunk.get(object5);
        if (iLookupThunk == object6) {
            __thunk__0__ = __site__0__.fault(object5);
            object6 = __thunk__0__.get(object5);
        }
        return iFn.invoke(object, iFn2.invoke(iFn3.invoke(object2, const__4, iFn4.invoke(object3, iFn5.invoke(object4, iFn6.invoke(object6)))), (Object)"-"));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$gensym_padding.invokeStatic(object2);
    }
}

