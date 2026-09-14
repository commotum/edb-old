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
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class integrity$log_storage_seq$children__22523$fn__22525
extends AFunction {
    Object olookup;
    Object root_id;
    Object uuid;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__3 = RT.keyword(null, (String)"type");
    public static final Keyword const__5 = RT.keyword(null, (String)"branch");
    public static final Keyword const__6 = RT.keyword(null, (String)"leaf");
    public static final Keyword const__7 = RT.keyword(null, (String)"seg");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public integrity$log_storage_seq$children__22523$fn__22525(Object object, Object object2, Object object3) {
        this.olookup = object;
        this.root_id = object2;
        this.uuid = object3;
    }

    public Object invoke(Object entry) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = entry;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object subseg = RT.get((Object)this_.olookup, (Object)object2);
        Object object3 = entry;
        entry = null;
        Object object4 = subseg;
        subseg = null;
        integrity$log_storage_seq$children__22523$fn__22525 this_ = null;
        return ((IFn)const__2.getRawRoot()).invoke(object3, (Object)const__3, (Object)(Util.equiv((Object)this_.uuid, (Object)this_.root_id) ? const__5 : const__6), (Object)const__7, object4);
    }
}

