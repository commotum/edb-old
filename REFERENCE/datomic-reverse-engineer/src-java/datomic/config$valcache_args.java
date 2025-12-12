/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class config$valcache_args
extends AFunction {
    public static final AFn const__2 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"path"), "datomic.valcachePath", RT.keyword(null, (String)"max-gb"), "datomic.valcacheMaxGb"});
    public static final Var const__3 = RT.var((String)"datomic.config", (String)"property-map");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__7 = RT.keyword(null, (String)"eviction-interval-secs");
    public static final Object const__8 = 10L;
    public static final Keyword const__9 = RT.keyword(null, (String)"eviction-threshold-mb");
    public static final Var const__10 = RT.var((String)"datomic.config", (String)"max-gb->eviction-threshold-mb");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"max-gb"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic() {
        Object object;
        AFn required = const__2;
        Object m = ((IFn)const__3.getRawRoot()).invoke((Object)required);
        AFn aFn = required;
        required = null;
        if ((long)RT.count((Object)m) == (long)RT.count((Object)aFn)) {
            IFn iFn = (IFn)const__6.getRawRoot();
            Object object2 = m;
            IFn iFn2 = (IFn)const__10.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = m;
            m = null;
            Object object4 = iLookupThunk.get(object3);
            if (iLookupThunk == object4) {
                __thunk__0__ = __site__0__.fault(object3);
                object4 = __thunk__0__.get(object3);
            }
            object = iFn.invoke(object2, (Object)const__7, const__8, (Object)const__9, iFn2.invoke(object4));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke() {
        return config$valcache_args.invokeStatic();
    }
}

