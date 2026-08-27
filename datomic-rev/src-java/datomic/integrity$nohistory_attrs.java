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
import datomic.integrity$nohistory_attrs$fn__21984;

public final class integrity$nohistory_attrs
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__3 = RT.keyword(null, (String)"id");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"instance?");
    public static final Object const__7 = RT.classForName((String)"datomic.db.Attribute");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"elements"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2) {
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        IFn iFn3 = (IFn)const__4.getRawRoot();
        integrity$nohistory_attrs$fn__21984 integrity$nohistory_attrs$fn__21984 = new integrity$nohistory_attrs$fn__21984();
        IFn iFn4 = (IFn)const__4.getRawRoot();
        Object object2 = ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot(), const__7);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = db2;
        db2 = null;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        return iFn.invoke(object, (Object)PersistentHashSet.EMPTY, iFn2.invoke((Object)const__3, iFn3.invoke((Object)integrity$nohistory_attrs$fn__21984, iFn4.invoke(object2, object4))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$nohistory_attrs.invokeStatic(object2);
    }
}

