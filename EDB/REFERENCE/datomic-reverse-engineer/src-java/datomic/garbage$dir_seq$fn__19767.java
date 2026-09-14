/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class garbage$dir_seq$fn__19767
extends AFunction {
    Object lookup;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"children"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public garbage$dir_seq$fn__19767(Object object) {
        this.lookup = object;
    }

    public Object invoke(Object root_entry) {
        ILookupThunk iLookupThunk = __thunk__1__;
        IFn iFn = (IFn)const__2.getRawRoot();
        ILookupThunk iLookupThunk2 = __thunk__0__;
        Object object = root_entry;
        root_entry = null;
        Object object2 = iLookupThunk2.get(object);
        if (iLookupThunk2 == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object object3 = RT.get((Object)this.lookup, (Object)iFn.invoke(object2));
        Object object4 = iLookupThunk.get(object3);
        if (iLookupThunk == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        return object4;
    }
}

