/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import java.util.Comparator;

public final class integrity$unsorted_dirs$fn__22040
extends AFunction {
    Object progress;
    Object cmp;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public integrity$unsorted_dirs$fn__22040(Object object, Object object2) {
        this.progress = object;
        this.cmp = object2;
    }

    public Object invoke(Object a, Object b) {
        ((IFn)this_.progress).invoke();
        Comparator comparator = (Comparator)this_.cmp;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = a;
        a = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = b;
        b = null;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        integrity$unsorted_dirs$fn__22040 this_ = null;
        return Numbers.lt((long)comparator.compare(object2, object4), (long)0L) ? Boolean.TRUE : Boolean.FALSE;
    }
}

