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

public final class integrity$validate_indexed_excisions$fn__22404
extends AFunction {
    Object db;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"datomic.integrity", (String)"e-ts");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"id"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"indexBasisT"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public integrity$validate_indexed_excisions$fn__22404(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__22401_SHARP_) {
        IFn iFn = (IFn)const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__22401_SHARP_;
        p1__22401_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object object3 = iFn.invoke(iFn2.invoke(this_.db, object2));
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object4 = this_.db;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        integrity$validate_indexed_excisions$fn__22404 this_ = null;
        return Numbers.lte((Object)object3, (Object)object5) ? Boolean.TRUE : Boolean.FALSE;
    }
}

