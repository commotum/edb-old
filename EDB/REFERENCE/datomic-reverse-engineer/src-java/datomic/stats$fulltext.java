/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.stats$fulltext$fn__17865;

public final class stats$fulltext
extends AFunction {
    public static final Var const__4 = RT.var((String)"datomic.stats", (String)"aevt");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"reduce");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"olookup"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"root"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"attrmap"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public static Object invokeStatic(Object db2, Object index2) {
        Object result2;
        Object aevt_summary;
        Object object;
        Object object2;
        Object object3;
        ILookupThunk iLookupThunk = __thunk__1__;
        ILookupThunk iLookupThunk2 = __thunk__0__;
        Object object4 = index2;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__0__ = __site__0__.fault(object4);
            object5 = __thunk__0__.get(object4);
        }
        if (iLookupThunk == (object3 = iLookupThunk.get(object5))) {
            __thunk__1__ = __site__1__.fault(object5);
            object3 = __thunk__1__.get(object5);
        }
        Object olookup = object3;
        ILookupThunk iLookupThunk3 = __thunk__4__;
        ILookupThunk iLookupThunk4 = __thunk__3__;
        ILookupThunk iLookupThunk5 = __thunk__2__;
        Object object6 = index2;
        Object object7 = iLookupThunk5.get(object6);
        if (iLookupThunk5 == object7) {
            __thunk__2__ = __site__2__.fault(object6);
            object7 = __thunk__2__.get(object6);
        }
        if (iLookupThunk4 == (object2 = iLookupThunk4.get(object7))) {
            __thunk__3__ = __site__3__.fault(object7);
            object2 = __thunk__3__.get(object7);
        }
        if (iLookupThunk3 == (object = iLookupThunk3.get(object2))) {
            __thunk__4__ = __site__4__.fault(object2);
            object = __thunk__4__.get(object2);
        }
        Object attrmap = object;
        Object object8 = index2;
        index2 = null;
        Object object9 = aevt_summary = ((IFn)const__4.getRawRoot()).invoke(db2, object8);
        aevt_summary = null;
        Object object10 = olookup;
        olookup = null;
        Object object11 = db2;
        db2 = null;
        Object object12 = attrmap;
        attrmap = null;
        Object object13 = result2 = ((IFn)const__5.getRawRoot()).invoke((Object)new stats$fulltext$fn__17865(object9, object10, object11), (Object)PersistentArrayMap.EMPTY, object12);
        result2 = null;
        return object13;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return stats$fulltext.invokeStatic(object3, object4);
    }
}

