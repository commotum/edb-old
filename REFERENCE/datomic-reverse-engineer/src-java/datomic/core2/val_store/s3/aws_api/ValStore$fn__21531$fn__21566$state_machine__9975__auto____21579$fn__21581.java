/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  cognitect.caster.Impl
 */
package datomic.core2.val_store.s3.aws_api;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import cognitect.caster.Impl;
import java.nio.ByteBuffer;

public final class ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581
extends AFunction {
    Object G__21534;
    Object G__21535;
    Object G__21532;
    Object state_21565;
    Object old_frame__9976__auto__;
    Object G__21533;
    Object G__21537;
    Object G__21536;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Object const__18;
    public static final Var const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Keyword const__26;
    public static final Var const__27;
    public static final Keyword const__28;
    public static final AFn const__29;
    public static final Var const__30;
    public static final Keyword const__31;
    public static final Var const__32;
    public static final Var const__33;
    public static final Keyword const__34;
    public static final Keyword const__35;
    public static final Keyword const__37;
    public static final Keyword const__38;
    public static final Var const__40;
    public static final Var const__41;
    public static final Keyword const__43;

    public ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.G__21534 = object;
        this.G__21535 = object2;
        this.G__21532 = object3;
        this.state_21565 = object4;
        this.old_frame__9976__auto__ = object5;
        this.G__21533 = object6;
        this.G__21537 = object7;
        this.G__21536 = object8;
    }

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public Object invoke() {
        try {
            Var.resetThreadBindingFrame((Object)((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 3L));
            do {
                G__21582 = RT.intCast((Object)((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 1L));
                switch (G__21582) {
                    case 1: {
                        ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 6L);
                        ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 7L);
                        ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 8L);
                        ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 9L);
                        ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 10L);
                        ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 11L);
                        inst_21540 = ((IFn)this.G__21532).invoke();
                        inst_21541 = ((IFn)this.G__21533).invoke();
                        inst_21542 = ((IFn)this.G__21534).invoke();
                        inst_21543 = ((IFn)this.G__21535).invoke();
                        inst_21544 = ((IFn)this.G__21536).invoke();
                        inst_21545 = ((IFn)this.G__21537).invoke();
                        v0 = inst_21540;
                        inst_21540 = null;
                        inst_21546 = v0;
                        v1 = inst_21541;
                        inst_21541 = null;
                        inst_21547 = v1;
                        v2 = inst_21542;
                        inst_21542 = null;
                        inst_21548 = v2;
                        v3 = inst_21543;
                        inst_21543 = null;
                        inst_21549 = v3;
                        v4 = inst_21544;
                        inst_21544 = null;
                        inst_21550 = v4;
                        v5 = inst_21545;
                        inst_21545 = null;
                        inst_21551 = v5;
                        bucket = inst_21546;
                        client = inst_21548;
                        k = inst_21549;
                        prefix = inst_21550;
                        opts = inst_21551;
                        v6 = new Object[6];
                        v6[0] = ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__12;
                        v7 = bucket;
                        bucket = null;
                        v6[1] = v7;
                        v6[2] = ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__13;
                        v8 = client;
                        client = null;
                        v6[3] = v8;
                        v6[4] = ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__14;
                        v9 = prefix;
                        prefix = null;
                        v10 = k;
                        k = null;
                        v11 = opts;
                        opts = null;
                        v6[5] = ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__15.getRawRoot()).invoke(v9, v10, v11);
                        inst_21552 = ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__11.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])v6));
                        statearr_21583 = this.state_21565;
                        v12 = inst_21546;
                        inst_21546 = null;
                        ((IFn.OLOO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__16.getRawRoot()).invokePrim(statearr_21583, 6L, v12);
                        v13 = inst_21547;
                        inst_21547 = null;
                        ((IFn.OLOO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__16.getRawRoot()).invokePrim(statearr_21583, 7L, v13);
                        v14 = inst_21548;
                        inst_21548 = null;
                        ((IFn.OLOO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__16.getRawRoot()).invokePrim(statearr_21583, 8L, v14);
                        v15 = inst_21549;
                        inst_21549 = null;
                        ((IFn.OLOO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__16.getRawRoot()).invokePrim(statearr_21583, 9L, v15);
                        v16 = inst_21550;
                        inst_21550 = null;
                        ((IFn.OLOO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__16.getRawRoot()).invokePrim(statearr_21583, 10L, v16);
                        v17 = inst_21551;
                        inst_21551 = null;
                        ((IFn.OLOO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__16.getRawRoot()).invokePrim(statearr_21583, 11L, v17);
                        v18 = statearr_21583;
                        statearr_21583 = null;
                        v19 = state_21565 = v18;
                        state_21565 = null;
                        v20 = inst_21552;
                        inst_21552 = null;
                        v21 = ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__17.getRawRoot()).invoke(v19, ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__18, v20);
                        break;
                    }
                    case 2: {
                        inst_21546 = ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 6L);
                        inst_21547 = ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 7L);
                        inst_21548 = ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 8L);
                        inst_21549 = ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 9L);
                        inst_21550 = ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 10L);
                        inst_21551 = ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 11L);
                        v22 = inst_21554 = ((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 2L);
                        inst_21554 = null;
                        v23 = inst_21555 = v22;
                        inst_21555 = null;
                        v__19654__auto__21589 = v23;
                        v24 = or__5581__auto__21588 = ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__20.getRawRoot()).invoke(v__19654__auto__21589);
                        if (v24 != null && v24 != Boolean.FALSE) {
                            v25 = or__5581__auto__21588;
                            or__5581__auto__21588 = null;
                        } else {
                            v25 = v__19654__auto__21589;
                            v__19654__auto__21589 = null;
                        }
                        inst_21556 = v25;
                        map__21539 = inst_21556;
                        v26 = ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__21.getRawRoot()).invoke(map__21539);
                        if (v26 != null && v26 != Boolean.FALSE) {
                            v27 = ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__22.getRawRoot()).invoke(map__21539);
                            if (v27 != null && v27 != Boolean.FALSE) {
                                v28 = map__21539;
                                map__21539 = null;
                                v29 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__23.getRawRoot()).invoke(v28)));
                            } else {
                                v30 = ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__24.getRawRoot()).invoke(map__21539);
                                if (v30 != null && v30 != Boolean.FALSE) {
                                    v31 = map__21539;
                                    map__21539 = null;
                                    v29 = ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__25.getRawRoot()).invoke(v31);
                                } else {
                                    v29 = PersistentArrayMap.EMPTY;
                                }
                            }
                        } else {
                            v29 = map__21539;
                            map__21539 = null;
                        }
                        inst_21557 = v29;
                        map__21539 = inst_21557;
                        v32 = map__21539;
                        map__21539 = null;
                        inst_21558 = RT.get((Object)v32, (Object)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__26);
                        v33 = inst_21556;
                        inst_21556 = null;
                        inst_21559 = v33;
                        inst_21560 = inst_21557;
                        v34 = inst_21557;
                        inst_21557 = null;
                        inst_21561 = v34;
                        v35 = inst_21558;
                        inst_21558 = null;
                        inst_21562 = v35;
                        inst_21546 = null;
                        inst_21547 = null;
                        v36 = inst_21562;
                        inst_21562 = null;
                        value = v36;
                        inst_21548 = null;
                        inst_21549 = null;
                        inst_21550 = null;
                        inst_21560 = null;
                        v37 = inst_21561;
                        inst_21561 = null;
                        result = v37;
                        inst_21551 = null;
                        v38 = ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__27.getRawRoot()).invoke(result);
                        if (v38 == null || v38 == Boolean.FALSE) ** GOTO lbl212
                        v39 /* !! */  = ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__29;
                        ** GOTO lbl235
lbl212:
                        // 1 sources

                        v40 = ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__30.getRawRoot()).invoke(result);
                        if (v40 == null || v40 == Boolean.FALSE) ** GOTO lbl217
                        v39 /* !! */  = result;
                        result = null;
                        ** GOTO lbl235
lbl217:
                        // 1 sources

                        v41 = ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__31;
                        if (v41 == null || v41 == Boolean.FALSE) ** GOTO lbl234
                        v42 = ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__33.getRawRoot();
                        if (Util.classOf((Object)v42) == ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.__cached_class__0) ** GOTO lbl224
                        if (!(v42 instanceof Impl)) {
                            v42 = v42;
                            ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.__cached_class__0 = Util.classOf((Object)v42);
lbl224:
                            // 2 sources

                            v43 = ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__32.getRawRoot().invoke(v42, (Object)RT.mapUniqueKeys((Object[])new Object[]{ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__34, ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__35, ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__26, RT.count((Object)value), ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__37, ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__38}));
                        } else {
                            v43 = ((Impl)v42).metric_STAR_((Object)RT.mapUniqueKeys((Object[])new Object[]{ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__34, ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__35, ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__26, RT.count((Object)value), ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__37, ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__38}));
                        }
                        v44 = new Object[2];
                        v44[0] = ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__28;
                        v45 = value;
                        value = null;
                        v44[1] = ByteBuffer.wrap((byte[])v45);
                        v39 /* !! */  = RT.mapUniqueKeys((Object[])v44);
                        ** GOTO lbl235
lbl234:
                        // 1 sources

                        v39 /* !! */  = null;
lbl235:
                        // 4 sources

                        inst_21563 = v39 /* !! */ ;
                        statearr_21584 = this.state_21565;
                        v46 = inst_21559;
                        inst_21559 = null;
                        ((IFn.OLOO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__16.getRawRoot()).invokePrim(statearr_21584, 12L, v46);
                        v47 = statearr_21584;
                        statearr_21584 = null;
                        v48 = state_21565 = v47;
                        state_21565 = null;
                        v49 = inst_21563;
                        inst_21563 = null;
                        v21 = ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__40.getRawRoot()).invoke(v48, (Object)v49);
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__41.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__21582));
                    }
                }
                result__9978__auto__21590 = v21;
            } while (Util.identical((Object)result__9978__auto__21590, (Object)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__43));
            v50 = result__9978__auto__21590;
            result__9978__auto__21590 = null;
            var50_37 /* !! */  = v50;
        }
        catch (Throwable ex__9979__auto__) {
            statearr_21585 = this.state_21565;
            ((IFn.OLOO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__16.getRawRoot()).invokePrim(statearr_21585, 2L, (Object)ex__9979__auto__);
            v51 = ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__24.getRawRoot()).invoke(((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 4L));
            if (v51 == null || v51 == Boolean.FALSE) {
                ex__9979__auto__ = null;
                throw ex__9979__auto__;
            }
            statearr_21586 = this.state_21565;
            ((IFn.OLOO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__16.getRawRoot()).invokePrim(statearr_21586, 1L, ((IFn)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__25.getRawRoot()).invoke(((IFn.OLO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__0.getRawRoot()).invokePrim(this.state_21565, 4L)));
            var50_37 /* !! */  = ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__43;
        }
        finally {
            this.state_21565 = null;
            ((IFn.OLOO)ValStore$fn__21531$fn__21566$state_machine__9975__auto____21579$fn__21581.const__16.getRawRoot()).invokePrim(this.state_21565, 3L, Var.getThreadBindingFrame());
            this.old_frame__9976__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__9976__auto__);
        }
        return var50_37 /* !! */ ;
    }

    static {
        const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
        const__11 = RT.var((String)"datomic.core2.aws.s3.aws-api", (String)"get-bytes");
        const__12 = RT.keyword(null, (String)"bucket");
        const__13 = RT.keyword(null, (String)"client");
        const__14 = RT.keyword(null, (String)"key");
        const__15 = RT.var((String)"datomic.core2.val-store.s3", (String)"storage-key");
        const__16 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
        const__17 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
        const__18 = 2L;
        const__20 = RT.var((String)"datomic.core2.async", (String)"channel-closed-error");
        const__21 = RT.var((String)"clojure.core", (String)"seq?");
        const__22 = RT.var((String)"clojure.core", (String)"next");
        const__23 = RT.var((String)"clojure.core", (String)"to-array");
        const__24 = RT.var((String)"clojure.core", (String)"seq");
        const__25 = RT.var((String)"clojure.core", (String)"first");
        const__26 = RT.keyword(null, (String)"value");
        const__27 = RT.var((String)"datomic.core2.anomalies", (String)"not-found?");
        const__28 = RT.keyword(null, (String)"val");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"val"), null});
        const__30 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
        const__31 = RT.keyword(null, (String)"default");
        const__32 = RT.var((String)"cognitect.caster", (String)"metric*");
        const__33 = RT.var((String)"cognitect.caster", (String)"instance");
        const__34 = RT.keyword(null, (String)"name");
        const__35 = RT.keyword(null, (String)"s3.get.bytes");
        const__37 = RT.keyword(null, (String)"units");
        const__38 = RT.keyword(null, (String)"count");
        const__40 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
        const__41 = RT.var((String)"clojure.core", (String)"str");
        const__43 = RT.keyword(null, (String)"recur");
    }
}

