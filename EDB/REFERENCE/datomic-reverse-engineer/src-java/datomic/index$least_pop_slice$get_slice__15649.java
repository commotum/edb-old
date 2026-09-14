/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;

public final class index$least_pop_slice$get_slice__15649
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"offset"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"offset"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"offset"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public Object invoke(Object lo, Object hi) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = lo;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = hi;
        hi = null;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object5 = lo;
        Object object6 = iLookupThunk3.get(object5);
        if (iLookupThunk3 == object6) {
            __thunk__2__ = __site__2__.fault(object5);
            object6 = __thunk__2__.get(object5);
        }
        Number number = Numbers.unchecked_minus((Object)object4, (Object)object6);
        ILookupThunk iLookupThunk4 = __thunk__3__;
        Object object7 = lo;
        lo = null;
        Object object8 = iLookupThunk4.get(object7);
        if (iLookupThunk4 == object8) {
            __thunk__3__ = __site__3__.fault(object7);
            object8 = __thunk__3__.get(object7);
        }
        return Tuple.create((Object)object2, (Object)number, (Object)object8);
    }
}

