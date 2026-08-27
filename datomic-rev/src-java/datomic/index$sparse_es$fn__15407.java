/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
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
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class index$sparse_es$fn__15407
extends AFunction {
    Object es;
    Object olookup;
    Object idx;
    public static final Keyword const__4 = RT.keyword(null, (String)"key");
    public static final Var const__5 = RT.var((String)"datomic.cache", (String)"getx-uncached");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"retracting-datum");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__11 = RT.keyword(null, (String)"avet");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"not=");
    public static final Object const__13 = 0L;
    public static final Keyword const__14 = RT.keyword(null, (String)"else");
    public static final Var const__15 = RT.var((String)"datomic.index", (String)"mindiff");
    public static final Keyword const__16 = RT.keyword(null, (String)"raet");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__19 = RT.var((String)"datomic.common", (String)"compare");
    public static final Keyword const__20 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__21 = RT.keyword(null, (String)"aevt");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"last-d"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"last-d"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"last-d"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"segid"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public index$sparse_es$fn__15407(Object object, Object object2, Object object3) {
        this.es = object;
        this.olookup = object2;
        this.idx = object3;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object i) {
        block36: {
            block35: {
                e = RT.nth((Object)this.es, (int)RT.uncheckedIntCast((Object)((Number)i)));
                and__5236__auto__15411 = Numbers.isPos((Object)i);
                if (and__5236__auto__15411) {
                    v0 = index$sparse_es$fn__15407.__thunk__0__;
                    v1 = e;
                    v2 = v0.get(v1);
                    if (v0 == v2) {
                        index$sparse_es$fn__15407.__thunk__0__ = index$sparse_es$fn__15407.__site__0__.fault(v1);
                        v2 = index$sparse_es$fn__15407.__thunk__0__.get(v1);
                    }
                    v3 = or__5238__auto__15410 = v2;
                    if (v3 != null && v3 != Boolean.FALSE) {
                        v4 = or__5238__auto__15410;
                        or__5238__auto__15410 = null;
                    } else {
                        v5 = index$sparse_es$fn__15407.__thunk__1__;
                        v6 = RT.nth((Object)this.es, (int)RT.uncheckedIntCast((Object)Numbers.unchecked_dec((Object)i)));
                        v4 = v5.get(v6);
                        if (v5 == v4) {
                            index$sparse_es$fn__15407.__thunk__1__ = index$sparse_es$fn__15407.__site__1__.fault(v6);
                            v4 = index$sparse_es$fn__15407.__thunk__1__.get(v6);
                        }
                    }
                } else {
                    v4 = and__5236__auto__15411 != false ? Boolean.TRUE : Boolean.FALSE;
                }
                if (v4 == null || v4 == Boolean.FALSE) break block35;
                v7 = index$sparse_es$fn__15407.__thunk__2__;
                v8 = e;
                v9 = v7.get(v8);
                if (v7 == v9) {
                    index$sparse_es$fn__15407.__thunk__2__ = index$sparse_es$fn__15407.__site__2__.fault(v8);
                    v9 = index$sparse_es$fn__15407.__thunk__2__.get(v8);
                }
                d = v9;
                v10 = i;
                i = null;
                prev_e = RT.nth((Object)this.es, (int)RT.uncheckedIntCast((Object)Numbers.unchecked_dec((Object)v10)));
                v11 = index$sparse_es$fn__15407.__thunk__3__;
                v12 = prev_e;
                v13 = v11.get(v12);
                if (v11 == v13) {
                    index$sparse_es$fn__15407.__thunk__3__ = index$sparse_es$fn__15407.__site__3__.fault(v12);
                    v13 = index$sparse_es$fn__15407.__thunk__3__.get(v12);
                }
                v14 = or__5238__auto__15412 = v13;
                if (v14 != null && v14 != Boolean.FALSE) {
                    v15 = or__5238__auto__15412;
                    or__5238__auto__15412 = null;
                } else {
                    v16 = (IFn)index$sparse_es$fn__15407.const__5.getRawRoot();
                    v17 = index$sparse_es$fn__15407.__thunk__4__;
                    v18 = prev_e;
                    prev_e = null;
                    v19 = v17.get(v18);
                    if (v17 == v19) {
                        index$sparse_es$fn__15407.__thunk__4__ = index$sparse_es$fn__15407.__site__4__.fault(v18);
                        v19 = index$sparse_es$fn__15407.__thunk__4__.get(v18);
                    }
                    v20 = seg_data = v16.invoke(this.olookup, v19);
                    v21 = seg_data;
                    seg_data = null;
                    v15 = RT.nth((Object)v20, (int)RT.uncheckedIntCast((long)((long)RT.count((Object)v21) - 1L)));
                }
                prior = v15;
                maked = ((IDatum)d).isAssertion() != false ? index$sparse_es$fn__15407.const__8.getRawRoot() : index$sparse_es$fn__15407.const__9.getRawRoot();
                v22 = (IFn)index$sparse_es$fn__15407.const__10.getRawRoot();
                v23 = e;
                e = null;
                G__15408 = this.idx;
                switch (Util.hash((Object)G__15408) >> 20 & 3) {
                    case 0: {
                        if (G__15408 == index$sparse_es$fn__15407.const__11) {
                            v24 = ((IFn)index$sparse_es$fn__15407.const__12.getRawRoot()).invoke((Object)((IDatum)d).getA(), (Object)((IDatum)prior).getA());
                            if (v24 != null && v24 != Boolean.FALSE) {
                                v25 = maked;
                                maked = null;
                                v26 = d;
                                d = null;
                                v27 = ((IFn)v25).invoke(index$sparse_es$fn__15407.const__13, (Object)((IDatum)v26).getA(), null, index$sparse_es$fn__15407.const__13);
                                break;
                            }
                            v28 = index$sparse_es$fn__15407.const__14;
                            if (v28 != null && v28 != Boolean.FALSE) {
                                dv = ((IDatum)d).getV();
                                v29 = prior;
                                prior = null;
                                diff_v = ((IFn)index$sparse_es$fn__15407.const__15.getRawRoot()).invoke(((IDatum)v29).getV(), dv);
                                v30 = dv;
                                dv = null;
                                v31 = ((IFn)index$sparse_es$fn__15407.const__12.getRawRoot()).invoke(diff_v, v30);
                                if (v31 != null && v31 != Boolean.FALSE) {
                                    v32 = maked;
                                    maked = null;
                                    v33 = d;
                                    d = null;
                                    v34 = diff_v;
                                    diff_v = null;
                                    v27 = ((IFn)v32).invoke(index$sparse_es$fn__15407.const__13, (Object)((IDatum)v33).getA(), v34, index$sparse_es$fn__15407.const__13);
                                    break;
                                }
                                v27 = d;
                                d = null;
                                break;
                            }
                            v27 = null;
                            break;
                        }
                        ** GOTO lbl219
                    }
                    case 1: {
                        if (G__15408 == index$sparse_es$fn__15407.const__16) {
                            v35 = ((IFn)index$sparse_es$fn__15407.const__17.getRawRoot()).invoke((Object)(Numbers.isZero((long)((IFn.OOL)index$sparse_es$fn__15407.const__19.getRawRoot()).invokePrim(((IDatum)d).getV(), ((IDatum)prior).getV())) != false ? Boolean.TRUE : Boolean.FALSE));
                            if (v35 != null && v35 != Boolean.FALSE) {
                                v36 = maked;
                                maked = null;
                                v37 = d;
                                d = null;
                                v27 = ((IFn)v36).invoke(index$sparse_es$fn__15407.const__13, index$sparse_es$fn__15407.const__13, ((IDatum)v37).getV(), index$sparse_es$fn__15407.const__13);
                                break;
                            }
                            v38 = prior;
                            prior = null;
                            v39 = ((IFn)index$sparse_es$fn__15407.const__12.getRawRoot()).invoke((Object)((IDatum)d).getA(), (Object)((IDatum)v38).getA());
                            if (v39 != null && v39 != Boolean.FALSE) {
                                v40 = maked;
                                maked = null;
                                v41 = ((IDatum)d).getA();
                                v42 = d;
                                d = null;
                                v27 = ((IFn)v40).invoke(index$sparse_es$fn__15407.const__13, (Object)v41, ((IDatum)v42).getV(), index$sparse_es$fn__15407.const__13);
                                break;
                            }
                            v43 = index$sparse_es$fn__15407.const__14;
                            if (v43 != null && v43 != Boolean.FALSE) {
                                v27 = d;
                                d = null;
                                break;
                            }
                            v27 = null;
                            break;
                        }
                        ** GOTO lbl219
                    }
                    case 2: {
                        if (G__15408 == index$sparse_es$fn__15407.const__20) {
                            v44 = ((IFn)index$sparse_es$fn__15407.const__12.getRawRoot()).invoke((Object)Numbers.num((long)((IDatum)d).getE()), (Object)Numbers.num((long)((IDatum)prior).getE()));
                            if (v44 != null && v44 != Boolean.FALSE) {
                                v45 = maked;
                                maked = null;
                                v46 = d;
                                d = null;
                                v27 = ((IFn)v45).invoke((Object)Numbers.num((long)((IDatum)v46).getE()), index$sparse_es$fn__15407.const__13, null, index$sparse_es$fn__15407.const__13);
                                break;
                            }
                            v47 = ((IFn)index$sparse_es$fn__15407.const__12.getRawRoot()).invoke((Object)((IDatum)d).getA(), (Object)((IDatum)prior).getA());
                            if (v47 != null && v47 != Boolean.FALSE) {
                                v48 = maked;
                                maked = null;
                                v49 = Numbers.num((long)((IDatum)d).getE());
                                v50 = d;
                                d = null;
                                v27 = ((IFn)v48).invoke((Object)v49, (Object)((IDatum)v50).getA(), null, index$sparse_es$fn__15407.const__13);
                                break;
                            }
                            v51 = index$sparse_es$fn__15407.const__14;
                            if (v51 != null && v51 != Boolean.FALSE) {
                                dv = ((IDatum)d).getV();
                                v52 = prior;
                                prior = null;
                                diff_v = ((IFn)index$sparse_es$fn__15407.const__15.getRawRoot()).invoke(((IDatum)v52).getV(), dv);
                                v53 = dv;
                                dv = null;
                                v54 = ((IFn)index$sparse_es$fn__15407.const__12.getRawRoot()).invoke(diff_v, v53);
                                if (v54 != null && v54 != Boolean.FALSE) {
                                    v55 = maked;
                                    maked = null;
                                    v56 = Numbers.num((long)((IDatum)d).getE());
                                    v57 = d;
                                    d = null;
                                    v58 = diff_v;
                                    diff_v = null;
                                    v27 = ((IFn)v55).invoke((Object)v56, (Object)((IDatum)v57).getA(), v58, index$sparse_es$fn__15407.const__13);
                                    break;
                                }
                                v27 = d;
                                d = null;
                                break;
                            }
                            v27 = null;
                            break;
                        }
                        ** GOTO lbl219
                    }
                    case 3: {
                        if (G__15408 == index$sparse_es$fn__15407.const__21) {
                            v59 = ((IFn)index$sparse_es$fn__15407.const__12.getRawRoot()).invoke((Object)((IDatum)d).getA(), (Object)((IDatum)prior).getA());
                            if (v59 != null && v59 != Boolean.FALSE) {
                                v60 = maked;
                                maked = null;
                                v61 = d;
                                d = null;
                                v27 = ((IFn)v60).invoke(index$sparse_es$fn__15407.const__13, (Object)((IDatum)v61).getA(), null, index$sparse_es$fn__15407.const__13);
                                break;
                            }
                            v62 = ((IFn)index$sparse_es$fn__15407.const__12.getRawRoot()).invoke((Object)Numbers.num((long)((IDatum)d).getE()), (Object)Numbers.num((long)((IDatum)prior).getE()));
                            if (v62 != null && v62 != Boolean.FALSE) {
                                v63 = maked;
                                maked = null;
                                v64 = Numbers.num((long)((IDatum)d).getE());
                                v65 = d;
                                d = null;
                                v27 = ((IFn)v63).invoke((Object)v64, (Object)((IDatum)v65).getA(), null, index$sparse_es$fn__15407.const__13);
                                break;
                            }
                            v66 = index$sparse_es$fn__15407.const__14;
                            if (v66 != null && v66 != Boolean.FALSE) {
                                dv = ((IDatum)d).getV();
                                v67 = prior;
                                prior = null;
                                diff_v = ((IFn)index$sparse_es$fn__15407.const__15.getRawRoot()).invoke(((IDatum)v67).getV(), dv);
                                v68 = dv;
                                dv = null;
                                v69 = ((IFn)index$sparse_es$fn__15407.const__12.getRawRoot()).invoke(diff_v, v68);
                                if (v69 != null && v69 != Boolean.FALSE) {
                                    v70 = maked;
                                    maked = null;
                                    v71 = Numbers.num((long)((IDatum)d).getE());
                                    v72 = d;
                                    d = null;
                                    v73 = diff_v;
                                    diff_v = null;
                                    v27 = ((IFn)v70).invoke((Object)v71, (Object)((IDatum)v72).getA(), v73, index$sparse_es$fn__15407.const__13);
                                    break;
                                }
                                v27 = d;
                                d = null;
                                break;
                            }
                            v27 = null;
                            break;
                        }
                    }
lbl219:
                    // 6 sources

                    default: {
                        v74 = G__15408;
                        G__15408 = null;
                        throw (Throwable)new IllegalArgumentException((String)((IFn)index$sparse_es$fn__15407.const__22.getRawRoot()).invoke((Object)"No matching clause: ", v74));
                    }
                }
                this = null;
                v75 = v22.invoke(v23, (Object)index$sparse_es$fn__15407.const__4, v27);
                break block36;
            }
            v75 = e;
            var2_2 = null;
        }
        return v75;
    }
}

