/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;

public final class stats$sparse_v_count$fn__17833
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"v"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public Object invoke(Object n, Object de) {
        Object object;
        boolean bl;
        Object object2;
        ILookupThunk iLookupThunk = __thunk__1__;
        ILookupThunk iLookupThunk2 = __thunk__0__;
        Object object3 = de;
        de = null;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        if (iLookupThunk == (object2 = iLookupThunk.get(object4))) {
            __thunk__1__ = __site__1__.fault(object4);
            object2 = __thunk__1__.get(object4);
        }
        Object v = object2;
        boolean or__5238__auto__17835 = Util.identical((Object)v, null);
        if (or__5238__auto__17835) {
            bl = or__5238__auto__17835;
        } else {
            Object object5 = v;
            v = null;
            bl = Util.equiv((long)0L, (Object)object5);
        }
        if (bl) {
            Object object6 = n;
            n = null;
            stats$sparse_v_count$fn__17833 this_ = null;
            object = Numbers.add((Object)object6, (long)1L);
        } else {
            object = n;
            Object var1_1 = null;
        }
        return object;
    }
}

