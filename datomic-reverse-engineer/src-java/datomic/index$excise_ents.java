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
import datomic.index$excise_ents$fn__15515;
import datomic.index$excise_ents$fn__15517;

public final class index$excise_ents
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"filter");
    public static final Keyword const__1 = RT.keyword((String)"db", (String)"excise");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"take-while");
    public static final Var const__4 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__5 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__9 = RT.keyword(null, (String)"a");
    public static final Object const__10 = 15L;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"indexing"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object db2) {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        index$excise_ents$fn__15515 index$excise_ents$fn__15515 = new index$excise_ents$fn__15515(db2);
        IFn iFn3 = (IFn)const__3.getRawRoot();
        index$excise_ents$fn__15517 index$excise_ents$fn__15517 = new index$excise_ents$fn__15517();
        IFn iFn4 = (IFn)const__4.getRawRoot();
        IFn iFn5 = (IFn)const__5.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__1__;
        ILookupThunk iLookupThunk2 = __thunk__0__;
        Object object2 = db2;
        Object object3 = iLookupThunk2.get(object2);
        if (iLookupThunk2 == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (iLookupThunk == (object = iLookupThunk.get(object3))) {
            __thunk__1__ = __site__1__.fault(object3);
            object = __thunk__1__.get(object3);
        }
        Object object4 = db2;
        db2 = null;
        return iFn.invoke((Object)const__1, iFn2.invoke((Object)index$excise_ents$fn__15515, iFn3.invoke((Object)index$excise_ents$fn__15517, iFn4.invoke(iFn5.invoke(object, ((IFn)const__8.getRawRoot()).invoke(object4, (Object)const__9, const__10))))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$excise_ents.invokeStatic(object2);
    }
}

