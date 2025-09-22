/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class uri$fixup_uri_map
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"force-map-keywords");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__3 = RT.keyword(null, (String)"protocol");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"keyword");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"protocol"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        Object ret = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, object));
        IFn iFn = (IFn)const__2.getRawRoot();
        Object object2 = ret;
        IFn iFn2 = (IFn)const__4.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = ret;
        ret = null;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        return iFn.invoke(object2, (Object)const__3, iFn2.invoke(object4));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fixup_uri_map.invokeStatic(object2);
    }
}

