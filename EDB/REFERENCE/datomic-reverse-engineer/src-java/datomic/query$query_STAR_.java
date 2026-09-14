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
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class query$query_STAR_
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"timeout");
    public static final Var const__1 = RT.var((String)"datomic.query", (String)"mapify-query");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__4 = RT.var((String)"datomic.query", (String)"q*");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"timeout"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"query"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"args"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object query_map2) {
        Object qmap;
        Object object;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = query_map2;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object timeout = object3;
        IFn iFn = (IFn)const__1.getRawRoot();
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object4 = query_map2;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        Object G__19533 = iFn.invoke(object5);
        Object object6 = timeout;
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = G__19533;
            G__19533 = null;
            Object object8 = timeout;
            timeout = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object7, (Object)const__0, (Object)Tuple.create((Object)object8));
        } else {
            object = G__19533;
            qmap = null;
        }
        qmap = object;
        IFn iFn2 = (IFn)const__4.getRawRoot();
        Object object9 = qmap;
        qmap = null;
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object10 = query_map2;
        query_map2 = null;
        Object object11 = iLookupThunk3.get(object10);
        if (iLookupThunk3 == object11) {
            __thunk__2__ = __site__2__.fault(object10);
            object11 = __thunk__2__.get(object10);
        }
        return iFn2.invoke(object9, object11);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$query_STAR_.invokeStatic(object2);
    }
}

