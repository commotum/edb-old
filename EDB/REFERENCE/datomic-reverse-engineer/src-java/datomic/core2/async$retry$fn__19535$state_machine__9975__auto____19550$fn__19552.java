/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class async$retry$fn__19535$state_machine__9975__auto____19550$fn__19552
extends AFunction {
    Object G__19482;
    Object old_frame__9976__auto__;
    Object G__19480;
    Object state_19534;
    Object G__19485;
    Object G__19481;
    Object G__19483;
    Object G__19484;
    Object G__19486;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
    public static final Object const__1 = 3L;
    public static final Var const__5 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__6 = 6L;
    public static final Object const__7 = 7L;
    public static final Object const__8 = 8L;
    public static final Object const__9 = 9L;
    public static final Object const__10 = 10L;
    public static final Object const__11 = 11L;
    public static final Object const__12 = 12L;
    public static final Object const__13 = 13L;
    public static final Object const__14 = 2L;
    public static final Keyword const__15 = RT.keyword(null, (String)"recur");
    public static final Var const__17 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
    public static final Object const__18 = 4L;
    public static final Var const__20 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
    public static final Object const__23 = 5L;
    public static final Var const__25 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"put!");
    public static final Var const__31 = RT.var((String)"clojure.core.async", (String)"timeout");
    public static final Var const__37 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__39 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__40 = RT.var((String)"clojure.core", (String)"first");

    public async$retry$fn__19535$state_machine__9975__auto____19550$fn__19552(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.G__19482 = object;
        this.old_frame__9976__auto__ = object2;
        this.G__19480 = object3;
        this.state_19534 = object4;
        this.G__19485 = object5;
        this.G__19481 = object6;
        this.G__19483 = object7;
        this.G__19484 = object8;
        this.G__19486 = object9;
    }

    public Object invoke() {
        Object object;
        try {
            Object result__9978__auto__19572;
            Var.resetThreadBindingFrame((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 3L));
            do {
                Object object2;
                int G__19553 = RT.intCast((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 1L));
                switch (G__19553) {
                    case 1: {
                        Object state_19534;
                        Object inst_19489 = ((IFn)this.G__19480).invoke();
                        Object inst_19490 = ((IFn)this.G__19481).invoke();
                        Object inst_19491 = ((IFn)this.G__19482).invoke();
                        Object inst_19492 = ((IFn)this.G__19483).invoke();
                        Object inst_19493 = ((IFn)this.G__19484).invoke();
                        Object inst_19494 = ((IFn)this.G__19485).invoke();
                        Object inst_19495 = ((IFn)this.G__19486).invoke();
                        Object object3 = inst_19489;
                        inst_19489 = null;
                        Object inst_19496 = object3;
                        Object object4 = inst_19490;
                        inst_19490 = null;
                        Object inst_19497 = object4;
                        Object object5 = inst_19491;
                        inst_19491 = null;
                        Object inst_19498 = object5;
                        Object object6 = inst_19492;
                        inst_19492 = null;
                        Object inst_19499 = object6;
                        Object object7 = inst_19493;
                        inst_19493 = null;
                        Object inst_19500 = object7;
                        Object object8 = inst_19494;
                        inst_19494 = null;
                        Object inst_19501 = object8;
                        Object object9 = inst_19495;
                        inst_19495 = null;
                        Object inst_19502 = object9;
                        long inst_19503 = 1L;
                        Object statearr_19554 = this.state_19534;
                        Object object10 = inst_19496;
                        inst_19496 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19554, 6L, object10);
                        Object object11 = inst_19497;
                        inst_19497 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19554, 7L, object11);
                        Object object12 = inst_19498;
                        inst_19498 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19554, 8L, object12);
                        Object object13 = inst_19499;
                        inst_19499 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19554, 9L, object13);
                        Object object14 = inst_19500;
                        inst_19500 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19554, 10L, object14);
                        Object object15 = inst_19501;
                        inst_19501 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19554, 11L, object15);
                        Object object16 = inst_19502;
                        inst_19502 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19554, 12L, object16);
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19554, 13L, (Object)Numbers.num((long)inst_19503));
                        Object object17 = statearr_19554;
                        statearr_19554 = null;
                        Object object18 = state_19534 = object17;
                        state_19534 = null;
                        Object statearr_19555 = object18;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19555, 2L, null);
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19555, 1L, const__14);
                        object2 = const__15;
                        break;
                    }
                    case 2: {
                        Object inst_19505;
                        Object inst_19498;
                        Object object19 = inst_19498 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 8L);
                        inst_19498 = null;
                        Object object20 = inst_19505 = ((IFn)object19).invoke();
                        inst_19505 = null;
                        object2 = ((IFn)const__17.getRawRoot()).invoke(this.state_19534, const__18, object20);
                        break;
                    }
                    case 3: {
                        Object inst_19532;
                        Object object21 = inst_19532 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 2L);
                        inst_19532 = null;
                        object2 = ((IFn)const__20.getRawRoot()).invoke(this.state_19534, object21);
                        break;
                    }
                    case 4: {
                        Object inst_19507;
                        Object inst_19499 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 9L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 14L);
                        Object object22 = inst_19507 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 2L);
                        inst_19507 = null;
                        Object inst_19508 = object22;
                        Object object23 = inst_19499;
                        inst_19499 = null;
                        Object inst_19509 = ((IFn)object23).invoke(inst_19508);
                        Object statearr_19556 = this.state_19534;
                        Object object24 = inst_19508;
                        inst_19508 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19556, 14L, object24);
                        Object object25 = statearr_19556;
                        statearr_19556 = null;
                        Object state_19534 = object25;
                        Object object26 = inst_19509;
                        inst_19509 = null;
                        if (object26 != null && object26 != Boolean.FALSE) {
                            Object object27 = state_19534;
                            state_19534 = null;
                            Object statearr_19557 = object27;
                            ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19557, 1L, const__23);
                        } else {
                            Object object28 = state_19534;
                            state_19534 = null;
                            Object statearr_19558 = object28;
                            ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19558, 1L, const__6);
                        }
                        object2 = const__15;
                        break;
                    }
                    case 5: {
                        Object inst_19501 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 11L);
                        Object inst_19508 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 14L);
                        Object object29 = inst_19501;
                        inst_19501 = null;
                        Object object30 = inst_19508;
                        inst_19508 = null;
                        object2 = ((IFn)const__25.getRawRoot()).invoke(this.state_19534, const__8, object29, object30);
                        break;
                    }
                    case 6: {
                        Object inst_19514;
                        Object inst_19500 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 10L);
                        Object inst_19503 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 13L);
                        Object inst_19508 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 14L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 15L);
                        Object object31 = inst_19500;
                        inst_19500 = null;
                        Object object32 = inst_19503;
                        inst_19503 = null;
                        Object object33 = inst_19508;
                        inst_19508 = null;
                        Object object34 = inst_19514 = ((IFn)object31).invoke(object32, object33);
                        inst_19514 = null;
                        Object inst_19515 = object34;
                        Object statearr_19559 = this.state_19534;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19559, 15L, inst_19515);
                        Object object35 = statearr_19559;
                        statearr_19559 = null;
                        Object state_19534 = object35;
                        Object object36 = inst_19515;
                        inst_19515 = null;
                        if (object36 != null && object36 != Boolean.FALSE) {
                            Object object37 = state_19534;
                            state_19534 = null;
                            Object statearr_19560 = object37;
                            ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19560, 1L, const__9);
                        } else {
                            Object object38 = state_19534;
                            state_19534 = null;
                            Object statearr_19561 = object38;
                            ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19561, 1L, const__10);
                        }
                        object2 = const__15;
                        break;
                    }
                    case 7: {
                        Object inst_19530 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 2L);
                        Object statearr_19562 = this.state_19534;
                        Object object39 = inst_19530;
                        inst_19530 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19562, 2L, object39);
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19562, 1L, const__1);
                        object2 = const__15;
                        break;
                    }
                    case 8: {
                        Object inst_19512 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 2L);
                        Object statearr_19563 = this.state_19534;
                        Object object40 = inst_19512;
                        inst_19512 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19563, 2L, object40);
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19563, 1L, const__7);
                        object2 = const__15;
                        break;
                    }
                    case 9: {
                        Object inst_19518;
                        Object inst_19517;
                        Object inst_19515;
                        Object object41 = inst_19515 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 15L);
                        inst_19515 = null;
                        Object object42 = inst_19517 = object41;
                        inst_19517 = null;
                        Object object43 = inst_19518 = ((IFn.LO)const__31.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object42)));
                        inst_19518 = null;
                        object2 = ((IFn)const__17.getRawRoot()).invoke(this.state_19534, const__12, object43);
                        break;
                    }
                    case 10: {
                        Object inst_19515 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 15L);
                        Object inst_19496 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 6L);
                        Object inst_19499 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 9L);
                        Object inst_195032 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 13L);
                        Object inst_19501 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 11L);
                        Object inst_19502 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 12L);
                        Object inst_19508 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 14L);
                        Object inst_19497 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 7L);
                        Object inst_19500 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 10L);
                        Object inst_19498 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 8L);
                        inst_19515 = null;
                        inst_19496 = null;
                        inst_19499 = null;
                        inst_195032 = null;
                        Object object44 = inst_19502;
                        inst_19502 = null;
                        Object fail2 = object44;
                        Object object45 = inst_19508;
                        inst_19508 = null;
                        Object result2 = object45;
                        inst_19497 = null;
                        inst_19500 = null;
                        inst_19498 = null;
                        Object object46 = fail2;
                        fail2 = null;
                        Object object47 = result2;
                        result2 = null;
                        Object inst_19524 = ((IFn)object46).invoke(object47);
                        Object object48 = inst_19501;
                        inst_19501 = null;
                        Object object49 = inst_19524;
                        inst_19524 = null;
                        object2 = ((IFn)const__25.getRawRoot()).invoke(this.state_19534, const__13, object48, object49);
                        break;
                    }
                    case 11: {
                        Object inst_19528 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 2L);
                        Object statearr_19564 = this.state_19534;
                        Object object50 = inst_19528;
                        inst_19528 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19564, 2L, object50);
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19564, 1L, const__7);
                        object2 = const__15;
                        break;
                    }
                    case 12: {
                        Object state_19534;
                        Number inst_19521;
                        Object inst_19503 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 13L);
                        Object inst_19520 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 2L);
                        Object object51 = inst_19503;
                        inst_19503 = null;
                        Number number = inst_19521 = Numbers.inc((Object)object51);
                        inst_19521 = null;
                        Object inst_195032 = number;
                        Object statearr_19565 = this.state_19534;
                        Object object52 = inst_19520;
                        inst_19520 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19565, 16L, object52);
                        Object object53 = inst_195032;
                        inst_195032 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19565, 13L, object53);
                        Object object54 = statearr_19565;
                        statearr_19565 = null;
                        Object object55 = state_19534 = object54;
                        state_19534 = null;
                        Object statearr_19566 = object55;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19566, 2L, null);
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19566, 1L, const__14);
                        object2 = const__15;
                        break;
                    }
                    case 13: {
                        Object inst_19526 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 2L);
                        Object statearr_19567 = this.state_19534;
                        Object object56 = inst_19526;
                        inst_19526 = null;
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19567, 2L, object56);
                        ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19567, 1L, const__11);
                        object2 = const__15;
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)const__37.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__19553));
                    }
                }
                result__9978__auto__19572 = object2;
            } while (Util.identical((Object)result__9978__auto__19572, (Object)const__15));
            Object object57 = result__9978__auto__19572;
            result__9978__auto__19572 = null;
            object = object57;
        }
        catch (Throwable ex__9979__auto__2) {
            Object statearr_19568 = this.state_19534;
            ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19568, 2L, (Object)ex__9979__auto__2);
            Object object58 = ((IFn)const__39.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 4L));
            if (object58 == null || object58 == Boolean.FALSE) {
                Object ex__9979__auto__2 = null;
                throw ex__9979__auto__2;
            }
            Object statearr_19569 = this.state_19534;
            ((IFn.OLOO)const__5.getRawRoot()).invokePrim(statearr_19569, 1L, ((IFn)const__40.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19534, 4L)));
            object = const__15;
        }
        finally {
            this.state_19534 = null;
            ((IFn.OLOO)const__5.getRawRoot()).invokePrim(this.state_19534, 3L, Var.getThreadBindingFrame());
            this.old_frame__9976__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__9976__auto__);
        }
        return object;
    }
}

