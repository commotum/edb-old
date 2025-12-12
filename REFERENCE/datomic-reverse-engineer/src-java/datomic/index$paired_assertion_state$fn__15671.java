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

public final class index$paired_assertion_state$fn__15671
extends AFunction {
    Object G__15667;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"next");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public index$paired_assertion_state$fn__15671(Object object) {
        this.G__15667 = object;
    }

    public Object invoke() {
        Object more;
        Object G__15667 = this.G__15667 = null;
        while (true) {
            Object object = G__15667;
            G__15667 = null;
            Object vec__15672 = object;
            Object sd1 = RT.nth((Object)vec__15672, (int)RT.uncheckedIntCast((long)0L), null);
            Object sd2 = RT.nth((Object)vec__15672, (int)RT.uncheckedIntCast((long)1L), null);
            Object object2 = vec__15672;
            vec__15672 = null;
            more = object2;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = sd1;
            sd1 = null;
            Object object4 = iLookupThunk.get(object3);
            if (iLookupThunk == object4) {
                __thunk__0__ = __site__0__.fault(object3);
                object4 = __thunk__0__.get(object3);
            }
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = sd2;
            sd2 = null;
            Object object6 = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object6) {
                __thunk__1__ = __site__1__.fault(object5);
                object6 = __thunk__1__.get(object5);
            }
            if (!Util.equiv((Object)object4, (Object)object6)) break;
            Object object7 = more;
            more = null;
            G__15667 = ((IFn)const__5.getRawRoot()).invoke(object7);
        }
        Object object = more;
        more = null;
        return object;
    }
}

