/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class clusterfs$describe$fn__14279
extends AFunction {
    Object cfs;
    public static final Var const__0 = RT.var((String)"datomic.clusterfs", (String)"ceil");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"length"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"chunk-size"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public clusterfs$describe$fn__14279(Object object) {
        this.cfs = object;
    }

    public Object invoke(Object p1__14278_SHARP_) {
        IFn.OL oL = (IFn.OL)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__14278_SHARP_;
        p1__14278_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        double d = RT.floatCast((Object)object2);
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = this.cfs;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        return Numbers.num((long)oL.invokePrim((Object)Numbers.divide((double)d, (Object)object4)));
    }
}

