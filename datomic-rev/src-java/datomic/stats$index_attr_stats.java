/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.stats$index_attr_stats$fn__17872;
import datomic.stats$index_attr_stats$fn__17876;
import datomic.stats$index_attr_stats$fn__17879;
import datomic.stats$index_attr_stats$fn__17882;

public final class stats$index_attr_stats
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.stats", (String)"storage-tiers");
    public static final Var const__1 = RT.var((String)"datomic.core2.algo.lazy", (String)"fred");
    public static final Var const__2 = RT.var((String)"datomic.core2.algo.lazy", (String)"fully-partition-by");
    public static final Var const__4 = RT.var((String)"datomic.stats", (String)"memory-tiers");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object db2, Object tier) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(tier);
        if (object2 != null && object2 != Boolean.FALSE) {
            IFn iFn = (IFn)const__1.getRawRoot();
            stats$index_attr_stats$fn__17872 stats$index_attr_stats$fn__17872 = new stats$index_attr_stats$fn__17872(db2);
            IFn iFn2 = (IFn)const__2.getRawRoot();
            stats$index_attr_stats$fn__17876 stats$index_attr_stats$fn__17876 = new stats$index_attr_stats$fn__17876();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = tier;
            tier = null;
            Object object4 = db2;
            db2 = null;
            Object object5 = ((IFn)object3).invoke(object4);
            Object object6 = iLookupThunk.get(object5);
            if (iLookupThunk == object6) {
                __thunk__0__ = __site__0__.fault(object5);
                object6 = __thunk__0__.get(object5);
            }
            object = iFn.invoke((Object)stats$index_attr_stats$fn__17872, (Object)PersistentArrayMap.EMPTY, iFn2.invoke((Object)stats$index_attr_stats$fn__17876, object6));
        } else {
            Object object7 = ((IFn)const__4.getRawRoot()).invoke(tier);
            if (object7 != null && object7 != Boolean.FALSE) {
                IFn iFn = (IFn)const__1.getRawRoot();
                stats$index_attr_stats$fn__17879 stats$index_attr_stats$fn__17879 = new stats$index_attr_stats$fn__17879(db2);
                IFn iFn3 = (IFn)const__2.getRawRoot();
                stats$index_attr_stats$fn__17882 stats$index_attr_stats$fn__17882 = new stats$index_attr_stats$fn__17882();
                ILookupThunk iLookupThunk = __thunk__1__;
                Object object8 = tier;
                tier = null;
                Object object9 = db2;
                db2 = null;
                Object object10 = ((IFn)object8).invoke(object9);
                Object object11 = iLookupThunk.get(object10);
                if (iLookupThunk == object11) {
                    __thunk__1__ = __site__1__.fault(object10);
                    object11 = __thunk__1__.get(object10);
                }
                object = iFn.invoke((Object)stats$index_attr_stats$fn__17879, (Object)PersistentArrayMap.EMPTY, iFn3.invoke((Object)stats$index_attr_stats$fn__17882, object11));
            } else {
                object = null;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return stats$index_attr_stats.invokeStatic(object3, object4);
    }
}

