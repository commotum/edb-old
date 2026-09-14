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
import datomic.clusterfs$describe$fn__14279;

public final class clusterfs$describe
extends AFunction {
    public static final Keyword const__1 = RT.keyword(null, (String)"file-count");
    public static final Keyword const__3 = RT.keyword(null, (String)"seg-count");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"vals");
    public static final Keyword const__8 = RT.keyword(null, (String)"byte-count");
    public static final Keyword const__9 = RT.keyword(null, (String)"length");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"dir"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object cfs) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = cfs;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object d = object2;
        Object[] objectArray = new Object[6];
        objectArray[0] = const__1;
        objectArray[1] = RT.count((Object)d);
        objectArray[2] = const__3;
        Object object3 = cfs;
        cfs = null;
        objectArray[3] = ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)new clusterfs$describe$fn__14279(object3), ((IFn)const__7.getRawRoot()).invoke(d)));
        objectArray[4] = const__8;
        Object object4 = d;
        d = null;
        objectArray[5] = ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__9, ((IFn)const__7.getRawRoot()).invoke(object4)));
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return clusterfs$describe.invokeStatic(object2);
    }
}

