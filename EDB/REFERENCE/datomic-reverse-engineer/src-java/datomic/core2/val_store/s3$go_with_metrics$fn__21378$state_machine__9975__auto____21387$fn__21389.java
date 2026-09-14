/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LD
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  cognitect.caster.Impl
 */
package datomic.core2.val_store;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import cognitect.caster.Impl;

public final class s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389
extends AFunction {
    Object old_frame__9976__auto__;
    Object G__21355;
    Object G__21353;
    Object state_21377;
    Object G__21354;
    Object G__21352;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__10;
    public static final Var const__11;
    public static final Object const__12;
    public static final Var const__14;
    public static final Keyword const__16;
    public static final Var const__17;
    public static final Keyword const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final Keyword const__21;
    public static final Var const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Keyword const__26;
    public static final Var const__27;
    public static final Keyword const__28;
    public static final Keyword const__29;
    public static final Keyword const__30;
    public static final Var const__31;
    public static final Var const__32;
    public static final Var const__33;
    public static final AFn const__35;
    public static final Var const__36;
    public static final Var const__37;
    public static final Keyword const__39;
    public static final Var const__40;
    public static final Var const__42;

    public s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.old_frame__9976__auto__ = object;
        this.G__21355 = object2;
        this.G__21353 = object3;
        this.state_21377 = object4;
        this.G__21354 = object5;
        this.G__21352 = object6;
    }

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public Object invoke() {
        try {
            Var.resetThreadBindingFrame((Object)((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 3L));
            do {
                G__21390 = RT.intCast((Object)((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 1L));
                switch (G__21390) {
                    case 1: {
                        ((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 6L);
                        ((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 7L);
                        ((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 8L);
                        ((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 9L);
                        ((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 10L);
                        inst_21357 = ((IFn)this.G__21352).invoke();
                        inst_21358 = ((IFn)this.G__21353).invoke();
                        inst_21359 = ((IFn)this.G__21354).invoke();
                        inst_21360 = ((IFn)this.G__21355).invoke();
                        v0 = inst_21357;
                        inst_21357 = null;
                        inst_21361 = v0;
                        v1 = inst_21358;
                        inst_21358 = null;
                        inst_21362 = v1;
                        v2 = inst_21359;
                        inst_21359 = null;
                        inst_21363 = v2;
                        v3 = inst_21360;
                        inst_21360 = null;
                        inst_21364 = v3;
                        start_nsec = inst_21365 = System.nanoTime();
                        v4 = f = inst_21361;
                        f = null;
                        inst_21366 = ((IFn)v4).invoke();
                        statearr_21391 = this.state_21377;
                        v5 = inst_21361;
                        inst_21361 = null;
                        ((IFn.OLOO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__10.getRawRoot()).invokePrim(statearr_21391, 6L, v5);
                        v6 = inst_21362;
                        inst_21362 = null;
                        ((IFn.OLOO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__10.getRawRoot()).invokePrim(statearr_21391, 7L, v6);
                        v7 = inst_21363;
                        inst_21363 = null;
                        ((IFn.OLOO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__10.getRawRoot()).invokePrim(statearr_21391, 8L, v7);
                        v8 = inst_21364;
                        inst_21364 = null;
                        ((IFn.OLOO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__10.getRawRoot()).invokePrim(statearr_21391, 9L, v8);
                        ((IFn.OLOO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__10.getRawRoot()).invokePrim(statearr_21391, 10L, (Object)Numbers.num((long)inst_21365));
                        v9 = statearr_21391;
                        statearr_21391 = null;
                        v10 = state_21377 = v9;
                        state_21377 = null;
                        v11 = inst_21366;
                        inst_21366 = null;
                        v12 = ((IFn)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__11.getRawRoot()).invoke(v10, s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__12, v11);
                        break;
                    }
                    case 2: {
                        inst_21365 = ((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 10L);
                        inst_21363 = ((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 8L);
                        inst_21362 = ((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 7L);
                        inst_21364 = ((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 9L);
                        inst_21361 = ((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 6L);
                        v13 = inst_21368 = ((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 2L);
                        inst_21368 = null;
                        v14 = inst_21369 = v13;
                        inst_21369 = null;
                        v__19654__auto__21396 = v14;
                        v15 = or__5581__auto__21395 = ((IFn)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__14.getRawRoot()).invoke(v__19654__auto__21396);
                        if (v15 != null && v15 != Boolean.FALSE) {
                            v16 = or__5581__auto__21395;
                            or__5581__auto__21395 = null;
                        } else {
                            v16 = v__19654__auto__21396;
                            v__19654__auto__21396 = null;
                        }
                        inst_21370 = v16;
                        start_nsec = inst_21365;
                        v17 = start_nsec;
                        start_nsec = null;
                        inst_21371 = Numbers.minus((long)System.nanoTime(), (Object)v17);
                        v18 = inst_21365;
                        inst_21365 = null;
                        inst_21372 = v18;
                        v19 = inst_21370;
                        inst_21370 = null;
                        inst_21373 = v19;
                        v20 = inst_21371;
                        inst_21371 = null;
                        inst_21374 = v20;
                        inst_21372 = null;
                        v21 = inst_21374;
                        inst_21374 = null;
                        nsec = v21;
                        v22 = inst_21363;
                        inst_21363 = null;
                        op = v22;
                        v23 = inst_21362;
                        inst_21362 = null;
                        k = v23;
                        v24 = inst_21373;
                        inst_21373 = null;
                        result = v24;
                        v25 = inst_21364;
                        inst_21364 = null;
                        context = v25;
                        inst_21361 = null;
                        if (Util.equiv((Object)op, (Object)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__16)) {
                            ((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__17.getRawRoot()).invokePrim((Object)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__18, RT.longCast((Object)nsec));
                        }
                        if (Util.classOf((Object)(v26 = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__20.getRawRoot())) == s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.__cached_class__0) ** GOTO lbl145
                        if (!(v26 instanceof Impl)) {
                            v26 = v26;
                            s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.__cached_class__0 = Util.classOf((Object)v26);
lbl145:
                            // 2 sources

                            v27 = new Object[8];
                            v27[0] = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__21;
                            v28 = ((IFn)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__23.getRawRoot()).invoke(result);
                            v29 = op;
                            op = null;
                            v27[1] = RT.get((Object)(v28 != null && v28 != Boolean.FALSE ? s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__24.getRawRoot() : s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__25.getRawRoot()), (Object)v29);
                            v27[2] = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__26;
                            v30 = nsec;
                            nsec = null;
                            v27[3] = ((IFn.LD)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__27.getRawRoot()).invokePrim(RT.longCast((Object)v30));
                            v27[4] = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__28;
                            v27[5] = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__29;
                            v27[6] = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__30;
                            v31 = k;
                            k = null;
                            v27[7] = v31;
                            v32 = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__19.getRawRoot().invoke(v26, (Object)RT.mapUniqueKeys((Object[])v27));
                        } else {
                            v33 = new Object[8];
                            v33[0] = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__21;
                            v34 = ((IFn)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__23.getRawRoot()).invoke(result);
                            v35 = op;
                            op = null;
                            v33[1] = RT.get((Object)(v34 != null && v34 != Boolean.FALSE ? s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__24.getRawRoot() : s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__25.getRawRoot()), (Object)v35);
                            v33[2] = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__26;
                            v36 = nsec;
                            nsec = null;
                            v33[3] = ((IFn.LD)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__27.getRawRoot()).invokePrim(RT.longCast((Object)v36));
                            v33[4] = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__28;
                            v33[5] = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__29;
                            v33[6] = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__30;
                            v37 = k;
                            k = null;
                            v33[7] = v37;
                            v32 = ((Impl)v26).metric_STAR_((Object)RT.mapUniqueKeys((Object[])v33));
                        }
                        v38 = ((IFn)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__31.getRawRoot()).invoke(result);
                        if (v38 == null || v38 == Boolean.FALSE) ** GOTO lbl194
                        v39 = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__20.getRawRoot();
                        if (Util.classOf((Object)v39) == s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.__cached_class__1) ** GOTO lbl187
                        if (!(v39 instanceof Impl)) {
                            v39 = v39;
                            s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.__cached_class__1 = Util.classOf((Object)v39);
lbl187:
                            // 2 sources

                            v40 = context;
                            context = null;
                            v41 = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__32.getRawRoot().invoke(v39, ((IFn)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__33.getRawRoot()).invoke(result, v40, (Object)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__35));
                        } else {
                            v42 = context;
                            context = null;
                            v41 = ((Impl)v39).event_STAR_(((IFn)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__33.getRawRoot()).invoke(result, v42, (Object)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__35));
                        }
lbl194:
                        // 3 sources

                        v43 = result;
                        result = null;
                        v44 = inst_21375 = v43;
                        inst_21375 = null;
                        v12 = ((IFn)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__36.getRawRoot()).invoke(this.state_21377, v44);
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__37.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__21390));
                    }
                }
                result__9978__auto__21397 = v12;
            } while (Util.identical((Object)result__9978__auto__21397, (Object)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__39));
            v45 = result__9978__auto__21397;
            result__9978__auto__21397 = null;
            var38_33 /* !! */  = v45;
        }
        catch (Throwable ex__9979__auto__) {
            statearr_21392 = this.state_21377;
            ((IFn.OLOO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__10.getRawRoot()).invokePrim(statearr_21392, 2L, (Object)ex__9979__auto__);
            v46 = ((IFn)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__40.getRawRoot()).invoke(((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 4L));
            if (v46 == null || v46 == Boolean.FALSE) {
                ex__9979__auto__ = null;
                throw ex__9979__auto__;
            }
            statearr_21393 = this.state_21377;
            ((IFn.OLOO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__10.getRawRoot()).invokePrim(statearr_21393, 1L, ((IFn)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__42.getRawRoot()).invoke(((IFn.OLO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__0.getRawRoot()).invokePrim(this.state_21377, 4L)));
            var38_33 /* !! */  = s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__39;
        }
        finally {
            this.state_21377 = null;
            ((IFn.OLOO)s3$go_with_metrics$fn__21378$state_machine__9975__auto____21387$fn__21389.const__10.getRawRoot()).invokePrim(this.state_21377, 3L, Var.getThreadBindingFrame());
            this.old_frame__9976__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__9976__auto__);
        }
        return var38_33 /* !! */ ;
    }

    static {
        const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
        const__10 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
        const__11 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
        const__12 = 2L;
        const__14 = RT.var((String)"datomic.core2.async", (String)"channel-closed-error");
        const__16 = RT.keyword(null, (String)"get");
        const__17 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
        const__18 = RT.keyword(null, (String)"s3-ns");
        const__19 = RT.var((String)"cognitect.caster", (String)"metric*");
        const__20 = RT.var((String)"cognitect.caster", (String)"instance");
        const__21 = RT.keyword(null, (String)"name");
        const__23 = RT.var((String)"datomic.core2.val-store.spi", (String)"val-op-succeeded?");
        const__24 = RT.var((String)"datomic.core2.val-store.s3", (String)"success-metrics");
        const__25 = RT.var((String)"datomic.core2.val-store.s3", (String)"failure-metrics");
        const__26 = RT.keyword(null, (String)"value");
        const__27 = RT.var((String)"datomic.measure.io-stats", (String)"ns->ms");
        const__28 = RT.keyword(null, (String)"units");
        const__29 = RT.keyword(null, (String)"msec");
        const__30 = RT.keyword((String)"datomic.core2.val-store.s3", (String)"key");
        const__31 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
        const__32 = RT.var((String)"cognitect.caster", (String)"event*");
        const__33 = RT.var((String)"clojure.core", (String)"merge");
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"msg"), "S3 op failed"});
        const__36 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
        const__37 = RT.var((String)"clojure.core", (String)"str");
        const__39 = RT.keyword(null, (String)"recur");
        const__40 = RT.var((String)"clojure.core", (String)"seq");
        const__42 = RT.var((String)"clojure.core", (String)"first");
    }
}

