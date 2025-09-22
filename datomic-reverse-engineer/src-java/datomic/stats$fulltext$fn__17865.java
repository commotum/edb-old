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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class stats$fulltext$fn__17865
extends AFunction {
    Object aevt_summary;
    Object olookup;
    Object db;
    public static final Object const__1 = 0L;
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"resolve-kw");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__5 = RT.var((String)"datomic.clusterfs", (String)"describe");
    public static final Keyword const__7 = RT.keyword(null, (String)"data-count");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"data-count"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public stats$fulltext$fn__17865(Object object, Object object2, Object object3) {
        this.aevt_summary = object;
        this.olookup = object2;
        this.db = object3;
    }

    public Object invoke(Object m, Object p__17864) {
        Object object;
        Object or__5238__auto__17870;
        Object object2 = p__17864;
        p__17864 = null;
        Object vec__17866 = object2;
        Object k = RT.nth((Object)vec__17866, (int)RT.intCast((long)0L), null);
        Object object3 = vec__17866;
        vec__17866 = null;
        Object v = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        Object object4 = k;
        k = null;
        Object attr = ((IFn)const__3.getRawRoot()).invoke(this_.db, object4);
        IFn iFn = (IFn)const__4.getRawRoot();
        Object object5 = v;
        v = null;
        Object object6 = ((IFn)const__5.getRawRoot()).invoke(RT.get((Object)this_.olookup, (Object)object5));
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = ((IFn)attr).invoke(this_.aevt_summary);
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        Object object9 = or__5238__auto__17870 = object8;
        if (object9 != null && object9 != Boolean.FALSE) {
            object = or__5238__auto__17870;
            or__5238__auto__17870 = null;
        } else {
            object = const__1;
        }
        Object detail = iFn.invoke(object6, (Object)const__7, object);
        Object object10 = m;
        m = null;
        Object object11 = attr;
        attr = null;
        Object object12 = detail;
        detail = null;
        stats$fulltext$fn__17865 this_ = null;
        return ((IFn)const__4.getRawRoot()).invoke(object10, object11, object12);
    }
}

