/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class tools$uniques_in_ts$fn__21863
extends AFunction {
    Object log;
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"iget");
    public static final Var const__1 = RT.var((String)"datomic.log", (String)"seek-tx");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"data"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public tools$uniques_in_ts$fn__21863(Object object) {
        this.log = object;
    }

    public Object invoke(Object p1__21858_SHARP_) {
        Object object;
        Object tx = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this.log, p1__21858_SHARP_));
        Object object2 = p1__21858_SHARP_;
        p1__21858_SHARP_ = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object3 = tx;
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        if (Util.equiv((Object)object2, (Object)object4)) {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = tx;
            tx = null;
            object = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object) {
                __thunk__1__ = __site__1__.fault(object5);
                object = __thunk__1__.get(object5);
            }
        } else {
            object = null;
        }
        return object;
    }
}

