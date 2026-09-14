/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.Database;
import datomic.btset.IDataSet;
import datomic.stats$index_attr_splits$fn__17896;
import datomic.stats$index_attr_splits$fn__17899;
import datomic.stats$index_attr_splits$fn__17905;
import datomic.stats$index_attr_splits$fn__17907;
import datomic.stats$index_attr_splits$fn__17910;

public final class stats$index_attr_splits
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.stats", (String)"memory-tiers");
    public static final Var const__3 = RT.var((String)"datomic.iter", (String)"take-while");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__5 = RT.keyword(null, (String)"a");
    public static final Var const__6 = RT.var((String)"datomic.iter", (String)"reduce");
    public static final Var const__8 = RT.var((String)"datomic.iter", (String)"iget");
    public static final Object const__9 = 0L;
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__15 = RT.var((String)"datomic.stats", (String)"storage-tiers");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"drop-while");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"take-while");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"map");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"e"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object db2, Object tier, Object attr) {
        Object object;
        Object object2 = attr;
        attr = null;
        Object a = ((Database)db2).entid(object2);
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(tier);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object temp__5457__auto__17914;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object4 = tier;
            tier = null;
            Object object5 = ((IFn)object4).invoke(db2);
            Object object6 = iLookupThunk.get(object5);
            if (iLookupThunk == object6) {
                __thunk__0__ = __site__0__.fault(object5);
                object6 = __thunk__0__.get(object5);
            }
            Object object7 = temp__5457__auto__17914 = object6;
            if (object7 != null && object7 != Boolean.FALSE) {
                Object object8 = temp__5457__auto__17914;
                temp__5457__auto__17914 = null;
                Object idx = object8;
                long split_n = 1000L;
                stats$index_attr_splits$fn__17896 stats$index_attr_splits$fn__17896 = new stats$index_attr_splits$fn__17896(a);
                Object object9 = idx;
                idx = null;
                Object object10 = db2;
                db2 = null;
                Object object11 = a;
                a = null;
                Object diter = ((IFn)const__3.getRawRoot()).invoke((Object)stats$index_attr_splits$fn__17896, (Object)((IDataSet)object9).seek(((IFn)const__4.getRawRoot()).invoke(object10, (Object)const__5, object11)));
                IFn iFn = (IFn)const__6.getRawRoot();
                stats$index_attr_splits$fn__17899 stats$index_attr_splits$fn__17899 = new stats$index_attr_splits$fn__17899(split_n);
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object12 = ((IFn)const__8.getRawRoot()).invoke(diter);
                Object object13 = iLookupThunk2.get(object12);
                if (iLookupThunk2 == object13) {
                    __thunk__1__ = __site__1__.fault(object12);
                    object13 = __thunk__1__.get(object12);
                }
                Object object14 = diter;
                diter = null;
                Object vec__17893 = iFn.invoke((Object)stats$index_attr_splits$fn__17899, (Object)Tuple.create((Object)PersistentVector.EMPTY, (Object)object13, (Object)const__9), object14);
                Object ret = RT.nth((Object)vec__17893, (int)RT.intCast((long)0L), null);
                Object nexte = RT.nth((Object)vec__17893, (int)RT.intCast((long)1L), null);
                Object object15 = vec__17893;
                vec__17893 = null;
                Object c = RT.nth((Object)object15, (int)RT.intCast((long)2L), null);
                Object object16 = ret;
                ret = null;
                Object G__17904 = object16;
                if (Numbers.isPos((Object)c)) {
                    Object object17 = G__17904;
                    G__17904 = null;
                    Object object18 = nexte;
                    nexte = null;
                    Object object19 = c;
                    c = null;
                    object = ((IFn)const__14.getRawRoot()).invoke(object17, (Object)Tuple.create((Object)object18, (Object)object19));
                } else {
                    object = G__17904;
                    G__17904 = null;
                }
            } else {
                object = null;
            }
        } else {
            Object object20 = ((IFn)const__15.getRawRoot()).invoke(tier);
            if (object20 != null && object20 != Boolean.FALSE) {
                Object temp__5457__auto__17915;
                IFn iFn = (IFn)const__16.getRawRoot();
                ILookupThunk iLookupThunk = __thunk__2__;
                Object object21 = tier;
                tier = null;
                Object object22 = db2;
                db2 = null;
                Object object23 = ((IFn)object21).invoke(object22);
                Object object24 = iLookupThunk.get(object23);
                if (iLookupThunk == object24) {
                    __thunk__2__ = __site__2__.fault(object23);
                    object24 = __thunk__2__.get(object23);
                }
                Object object25 = temp__5457__auto__17915 = iFn.invoke(object24);
                if (object25 != null && object25 != Boolean.FALSE) {
                    Object object26 = temp__5457__auto__17915;
                    temp__5457__auto__17915 = null;
                    Object dirs = object26;
                    stats$index_attr_splits$fn__17905 stats$index_attr_splits$fn__17905 = new stats$index_attr_splits$fn__17905(a);
                    Object object27 = a;
                    a = null;
                    Object object28 = dirs;
                    dirs = null;
                    object = ((IFn)const__17.getRawRoot()).invoke((Object)PersistentVector.EMPTY, ((IFn)const__18.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke((Object)stats$index_attr_splits$fn__17905), ((IFn)const__20.getRawRoot()).invoke((Object)new stats$index_attr_splits$fn__17907(object27)), ((IFn)const__21.getRawRoot()).invoke((Object)new stats$index_attr_splits$fn__17910())), object28);
                } else {
                    object = null;
                }
            } else {
                object = null;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return stats$index_attr_splits.invokeStatic(object4, object5, object6);
    }
}

