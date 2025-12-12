/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Tuple;

public final class tools$retract
extends AFunction {
    public static final Keyword const__0 = RT.keyword((String)"db", (String)"retract");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"e"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"v"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object d) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = d;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = d;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object5 = d;
        d = null;
        Object object6 = iLookupThunk3.get(object5);
        if (iLookupThunk3 == object6) {
            __thunk__2__ = __site__2__.fault(object5);
            object6 = __thunk__2__.get(object5);
        }
        return Tuple.create((Object)const__0, (Object)object2, (Object)object4, (Object)object6);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$retract.invokeStatic(object2);
    }
}

