/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.query$q_STAR_$fn__19521;
import datomic.query$q_STAR_$fn__19523;

public final class query$q_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.query.support", (String)"parse-as");
    public static final Var const__5 = RT.var((String)"datomic.query", (String)"query-cache");
    public static final Var const__9 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__10 = RT.keyword((String)"db.error", (String)"too-few-inputs");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__12 = RT.var((String)"datomic.query", (String)"group-fv");
    public static final Var const__15 = RT.var((String)"datomic.datalog", (String)"qsqr");
    public static final Var const__17 = RT.var((String)"datomic.query", (String)"group-rel");
    public static final Var const__18 = RT.var((String)"datomic.query", (String)"sort-for-pull");
    public static final Var const__20 = RT.var((String)"datomic.query", (String)"pull-fv");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"map");
    public static final AFn const__25 = (AFn)Symbol.intern(null, (String)"one-value");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"ffirst");
    public static final AFn const__27 = (AFn)Symbol.intern(null, (String)"first-tuple");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"first");
    public static final AFn const__29 = (AFn)Symbol.intern(null, (String)"one-column");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"mapv");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"in"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"in"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"find"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"group"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"group"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"with"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"pull"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"find-bindings"));
    static ILookupThunk __thunk__7__ = __site__7__;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object query2, Object srcs) {
        IPersistentVector iPersistentVector;
        Object pfn;
        Object ret;
        block35: {
            Object object;
            block36: {
                Object object2;
                Object temp__5455__auto__19532;
                Object object3;
                AFunction aFunction;
                query$q_STAR_$fn__19521 or__5238__auto__19529;
                query$q_STAR_$fn__19523 query$q_STAR_$fn__19523;
                query$q_STAR_$fn__19521 query$q_STAR_$fn__19521;
                Object ret2;
                Object object4;
                Object object5;
                Object ret3;
                Object group_clause;
                Object qmap;
                Object as;
                block34: {
                    int and__5236__auto__19528;
                    block33: {
                        Object or__5238__auto__19527;
                        Object object6 = query2;
                        query2 = null;
                        Object vec__19518 = ((IFn)const__0.getRawRoot()).invoke(object6);
                        Object query3 = RT.nth((Object)vec__19518, (int)RT.intCast((long)0L), null);
                        Object object7 = vec__19518;
                        vec__19518 = null;
                        as = RT.nth((Object)object7, (int)RT.intCast((long)1L), null);
                        Object object8 = query3;
                        query3 = null;
                        qmap = RT.get((Object)const__5.getRawRoot(), (Object)object8);
                        ILookupThunk iLookupThunk = __thunk__0__;
                        Object object10 = qmap;
                        object10 = iLookupThunk.get(object10);
                        if (iLookupThunk == object10) {
                            __thunk__0__ = __site__0__.fault(object9);
                            object10 = __thunk__0__.get(object9);
                        }
                        if ((long)RT.count((Object)object10) <= (long)RT.count((Object)srcs)) {
                        } else {
                            IFn iFn2 = (IFn)const__9.getRawRoot();
                            iFn2 = (IFn)const__11.getRawRoot();
                            ILookupThunk iLookupThunk2 = __thunk__1__;
                            Object object12 = qmap;
                            object12 = iLookupThunk2.get(object12);
                            if (iLookupThunk2 == object12) {
                                __thunk__1__ = __site__1__.fault(object11);
                                object12 = __thunk__1__.get(object11);
                            }
                            iFn.invoke((Object)const__10, iFn2.invoke((Object)"Query expected ", (Object)RT.count((Object)object12), (Object)" inputs but received ", (Object)RT.count((Object)srcs)));
                        }
                        IFn iFn = (IFn)const__12.getRawRoot();
                        ILookupThunk iLookupThunk3 = __thunk__2__;
                        Object object14 = qmap;
                        object14 = iLookupThunk3.get(object14);
                        if (iLookupThunk3 == object14) {
                            __thunk__2__ = __site__2__.fault(object13);
                            object14 = __thunk__2__.get(object13);
                        }
                        ILookupThunk iLookupThunk4 = __thunk__3__;
                        Object object16 = qmap;
                        object16 = iLookupThunk4.get(object16);
                        if (iLookupThunk4 == object16) {
                            __thunk__3__ = __site__3__.fault(object15);
                            object16 = __thunk__3__.get(object15);
                        }
                        group_clause = iFn.invoke(object14, object16);
                        ret3 = ((IFn)const__15.getRawRoot()).invoke(srcs, qmap);
                        and__5236__auto__19528 = RT.count((Object)ret3);
                        Integer n = and__5236__auto__19528;
                        if (n == null || n == Boolean.FALSE) break block33;
                        ILookupThunk iLookupThunk5 = __thunk__4__;
                        Object object18 = qmap;
                        object18 = iLookupThunk5.get(object18);
                        if (iLookupThunk5 == object18) {
                            __thunk__4__ = __site__4__.fault(object17);
                            object18 = __thunk__4__.get(object17);
                        }
                        Object object19 = or__5238__auto__19527 = object18;
                        if (object19 != null && object19 != Boolean.FALSE) {
                            object5 = or__5238__auto__19527;
                            or__5238__auto__19527 = null;
                            break block34;
                        } else {
                            ILookupThunk iLookupThunk6 = __thunk__5__;
                            Object object5 = qmap;
                            object5 = iLookupThunk6.get(object5);
                            if (iLookupThunk6 == object5) {
                                __thunk__5__ = __site__5__.fault(object20);
                                object5 = __thunk__5__.get(object20);
                            }
                        }
                        break block34;
                    }
                    object5 = and__5236__auto__19528;
                }
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object object21 = group_clause;
                    group_clause = null;
                    Object object22 = ret3;
                    ret3 = null;
                    object4 = ((IFn)const__17.getRawRoot()).invoke(object21, object22);
                } else {
                    object4 = ret3;
                    ret3 = null;
                }
                Object object23 = ret2 = object4;
                ret2 = null;
                ret = ((IFn)const__18.getRawRoot()).invoke(qmap, object23);
                ILookupThunk iLookupThunk = __thunk__6__;
                Object object25 = qmap;
                object25 = iLookupThunk.get(object25);
                if (iLookupThunk == object25) {
                    __thunk__6__ = __site__6__.fault(object24);
                    object25 = __thunk__6__.get(object24);
                }
                if (object25 != null && object25 != Boolean.FALSE) {
                    Object object26 = srcs;
                    srcs = null;
                    Object fv = ((IFn)const__20.getRawRoot()).invoke(qmap, object26);
                    fv = null;
                    query$q_STAR_$fn__19521 = new query$q_STAR_$fn__19521(fv);
                } else {
                    query$q_STAR_$fn__19521 = null;
                }
                query$q_STAR_$fn__19521 pullfn = query$q_STAR_$fn__19521;
                Object object27 = as;
                if (object27 != null && object27 != Boolean.FALSE) {
                    as = null;
                    query$q_STAR_$fn__19523 = new query$q_STAR_$fn__19523(as);
                } else {
                    query$q_STAR_$fn__19523 = null;
                }
                query$q_STAR_$fn__19523 asfn = query$q_STAR_$fn__19523;
                query$q_STAR_$fn__19521 query$q_STAR_$fn__195212 = or__5238__auto__19529 = pullfn;
                if (query$q_STAR_$fn__195212 != null && query$q_STAR_$fn__195212 != Boolean.FALSE) {
                    aFunction = or__5238__auto__19529;
                    or__5238__auto__19529 = null;
                } else {
                    aFunction = asfn;
                }
                if (aFunction != null && aFunction != Boolean.FALSE) {
                    Object object28;
                    query$q_STAR_$fn__19521 or__5238__auto__19531;
                    Object object29;
                    query$q_STAR_$fn__19523 or__5238__auto__19530;
                    IFn iFn = (IFn)const__21.getRawRoot();
                    query$q_STAR_$fn__19523 query$q_STAR_$fn__195232 = asfn;
                    asfn = null;
                    query$q_STAR_$fn__19523 query$q_STAR_$fn__195233 = or__5238__auto__19530 = query$q_STAR_$fn__195232;
                    if (query$q_STAR_$fn__195233 != null && query$q_STAR_$fn__195233 != Boolean.FALSE) {
                        object29 = or__5238__auto__19530;
                        or__5238__auto__19530 = null;
                    } else {
                        object29 = const__22.getRawRoot();
                    }
                    query$q_STAR_$fn__19521 query$q_STAR_$fn__195213 = pullfn;
                    pullfn = null;
                    query$q_STAR_$fn__19521 query$q_STAR_$fn__195214 = or__5238__auto__19531 = query$q_STAR_$fn__195213;
                    if (query$q_STAR_$fn__195214 != null && query$q_STAR_$fn__195214 != Boolean.FALSE) {
                        object28 = or__5238__auto__19531;
                        or__5238__auto__19531 = null;
                    } else {
                        object28 = const__22.getRawRoot();
                    }
                    object3 = iFn.invoke(object29, object28);
                } else {
                    object3 = null;
                }
                pfn = object3;
                ILookupThunk iLookupThunk7 = __thunk__7__;
                Object object30 = qmap;
                qmap = null;
                Object object31 = iLookupThunk7.get(object30);
                if (iLookupThunk7 == object31) {
                    __thunk__7__ = __site__7__.fault(object30);
                    object31 = __thunk__7__.get(object30);
                }
                Object object32 = temp__5455__auto__19532 = object31;
                if (object32 == null || object32 == Boolean.FALSE) break block35;
                Object object33 = temp__5455__auto__19532;
                temp__5455__auto__19532 = null;
                Object fb = object33;
                Object object34 = pfn;
                if (object34 != null && object34 != Boolean.FALSE) {
                    Object object35 = pfn;
                    pfn = null;
                    Object object36 = ret;
                    ret = null;
                    object2 = ((IFn)const__24.getRawRoot()).invoke(object35, object36);
                } else {
                    object2 = ret;
                    ret = null;
                }
                Object ret4 = object2;
                Object object37 = fb;
                fb = null;
                Object G__19525 = object37;
                switch (Util.hash((Object)G__19525) >> 2 & 3) {
                    case 0: {
                        if (!Util.equiv((Object)G__19525, (Object)const__25)) break;
                        Object object38 = ret4;
                        ret4 = null;
                        object = ((IFn)const__26.getRawRoot()).invoke(object38);
                        break block36;
                    }
                    case 2: {
                        if (!Util.equiv((Object)G__19525, (Object)const__27)) break;
                        Object object39 = ret4;
                        ret4 = null;
                        object = ((IFn)const__28.getRawRoot()).invoke(object39);
                        break block36;
                    }
                    case 3: {
                        if (!Util.equiv((Object)G__19525, (Object)const__29)) break;
                        Object object40 = ret4;
                        ret4 = null;
                        object = ((IFn)const__30.getRawRoot()).invoke(const__28.getRawRoot(), object40);
                        break block36;
                    }
                }
                Object object41 = G__19525;
                G__19525 = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__11.getRawRoot()).invoke((Object)"No matching clause: ", object41));
            }
            iPersistentVector = Tuple.create((Object)object, null);
            return iPersistentVector;
        }
        Object object = ret;
        ret = null;
        Object object42 = pfn;
        pfn = null;
        iPersistentVector = Tuple.create((Object)object, (Object)object42);
        return iPersistentVector;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$q_STAR_.invokeStatic(object3, object4);
    }
}

