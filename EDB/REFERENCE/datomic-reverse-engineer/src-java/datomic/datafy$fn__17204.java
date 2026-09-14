/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.datafy$fn__17204$fn__17205;

public final class datafy$fn__17204
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__2 = RT.keyword(null, (String)"name");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__5 = RT.var((String)"clojure.reflect", (String)"reflect");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"members"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object c) {
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        IFn iFn3 = (IFn)const__3.getRawRoot();
        datafy$fn__17204$fn__17205 datafy$fn__17204$fn__17205 = new datafy$fn__17204$fn__17205(c);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = c;
        c = null;
        Object object2 = ((IFn)const__5.getRawRoot()).invoke(object);
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        return iFn.invoke((Object)PersistentHashSet.EMPTY, iFn2.invoke((Object)const__2, iFn3.invoke((Object)datafy$fn__17204$fn__17205, object3)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$fn__17204.invokeStatic(object2);
    }
}

