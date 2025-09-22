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
import datomic.datalog$remap_bounds$mapize__18815;

public final class datalog$remap_bounds
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"zipmap");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"range");
    public static final Keyword const__2 = RT.keyword(null, (String)"consts");
    public static final Keyword const__3 = RT.keyword(null, (String)"starts");
    public static final Keyword const__4 = RT.keyword(null, (String)"whiles");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"consts"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"starts"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"whiles"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object nb, Object args) {
        Object ia;
        Object object = args;
        args = null;
        Object object2 = ia = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(), object);
        ia = null;
        datalog$remap_bounds$mapize__18815 mapize = new datalog$remap_bounds$mapize__18815(object2);
        Object[] objectArray = new Object[6];
        objectArray[0] = const__2;
        IFn iFn = (IFn)mapize;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = nb;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        objectArray[1] = iFn.invoke(object4);
        objectArray[2] = const__3;
        IFn iFn2 = (IFn)mapize;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object5 = nb;
        Object object6 = iLookupThunk2.get(object5);
        if (iLookupThunk2 == object6) {
            __thunk__1__ = __site__1__.fault(object5);
            object6 = __thunk__1__.get(object5);
        }
        objectArray[3] = iFn2.invoke(object6);
        objectArray[4] = const__4;
        datalog$remap_bounds$mapize__18815 datalog$remap_bounds$mapize__18815 = mapize;
        mapize = null;
        IFn iFn3 = (IFn)datalog$remap_bounds$mapize__18815;
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object7 = nb;
        nb = null;
        Object object8 = iLookupThunk3.get(object7);
        if (iLookupThunk3 == object8) {
            __thunk__2__ = __site__2__.fault(object7);
            object8 = __thunk__2__.get(object7);
        }
        objectArray[5] = iFn3.invoke(object8);
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$remap_bounds.invokeStatic(object3, object4);
    }
}

