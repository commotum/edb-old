/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class async$put_all_BANG_$fn__19616$state_machine__9975__auto____19623$fn__19625
extends AFunction {
    Object G__19584;
    Object old_frame__9976__auto__;
    Object state_19615;
    Object G__19585;
    Object G__19583;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
    public static final Object const__1 = 3L;
    public static final Object const__5 = 6L;
    public static final Object const__6 = 7L;
    public static final Object const__7 = 8L;
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__9 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__10 = 9L;
    public static final Object const__11 = 2L;
    public static final Keyword const__12 = RT.keyword(null, (String)"recur");
    public static final Object const__14 = 4L;
    public static final Object const__15 = 5L;
    public static final Var const__17 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__20 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"put!");
    public static final Var const__22 = RT.var((String)"clojure.core.async", (String)"close!");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"next");
    public static final Object const__28 = 10L;
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"str");

    public async$put_all_BANG_$fn__19616$state_machine__9975__auto____19623$fn__19625(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.G__19584 = object;
        this.old_frame__9976__auto__ = object2;
        this.state_19615 = object3;
        this.G__19585 = object4;
        this.G__19583 = object5;
    }

    public Object invoke() {
        Object object;
        try {
            Object result__9978__auto__19642;
            Var.resetThreadBindingFrame((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 3L));
            do {
                Object object2;
                int G__19626 = RT.intCast((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 1L));
                switch (G__19626) {
                    case 1: {
                        Object state_19615;
                        Object inst_19594;
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 6L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 7L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 8L);
                        Object inst_19588 = ((IFn)this.G__19583).invoke();
                        Object inst_19589 = ((IFn)this.G__19584).invoke();
                        Object inst_19590 = ((IFn)this.G__19585).invoke();
                        Object object3 = inst_19588;
                        inst_19588 = null;
                        Object inst_19591 = object3;
                        Object object4 = inst_19589;
                        inst_19589 = null;
                        Object inst_19592 = object4;
                        Object object5 = inst_19590;
                        inst_19590 = null;
                        Object inst_19593 = object5;
                        Object coll = inst_19592;
                        Object object6 = coll;
                        coll = null;
                        Object object7 = inst_19594 = ((IFn)const__8.getRawRoot()).invoke(object6);
                        inst_19594 = null;
                        Object inst_19595 = object7;
                        Object statearr_19627 = this.state_19615;
                        Object object8 = inst_19591;
                        inst_19591 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19627, 6L, object8);
                        Object object9 = inst_19592;
                        inst_19592 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19627, 7L, object9);
                        Object object10 = inst_19593;
                        inst_19593 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19627, 8L, object10);
                        Object object11 = inst_19595;
                        inst_19595 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19627, 9L, object11);
                        Object object12 = statearr_19627;
                        statearr_19627 = null;
                        Object object13 = state_19615 = object12;
                        state_19615 = null;
                        Object statearr_19628 = object13;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19628, 2L, null);
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19628, 1L, const__11);
                        object2 = const__12;
                        break;
                    }
                    case 2: {
                        Object inst_19595;
                        Object object14 = inst_19595 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 9L);
                        inst_19595 = null;
                        if (object14 != null && object14 != Boolean.FALSE) {
                            Object statearr_19629 = this.state_19615;
                            ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19629, 1L, const__14);
                        } else {
                            Object statearr_19630 = this.state_19615;
                            ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19630, 1L, const__15);
                        }
                        object2 = const__12;
                        break;
                    }
                    case 3: {
                        Object inst_19613;
                        Object object15 = inst_19613 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 2L);
                        inst_19613 = null;
                        object2 = ((IFn)const__17.getRawRoot()).invoke(this.state_19615, object15);
                        break;
                    }
                    case 4: {
                        Object inst_19595 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 9L);
                        Object inst_19591 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 6L);
                        Object object16 = inst_19595;
                        inst_19595 = null;
                        Object inst_19598 = ((IFn)const__19.getRawRoot()).invoke(object16);
                        Object object17 = inst_19591;
                        inst_19591 = null;
                        Object object18 = inst_19598;
                        inst_19598 = null;
                        object2 = ((IFn)const__20.getRawRoot()).invoke(this.state_19615, const__6, object17, object18);
                        break;
                    }
                    case 5: {
                        Object inst_19591 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 6L);
                        Object inst_19592 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 7L);
                        Object inst_19593 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 8L);
                        Object inst_19595 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 9L);
                        Object object19 = inst_19591;
                        inst_19591 = null;
                        Object ch = object19;
                        inst_19592 = null;
                        Object object20 = inst_19593;
                        inst_19593 = null;
                        Object close_QMARK_ = object20;
                        inst_19595 = null;
                        Object object21 = close_QMARK_;
                        close_QMARK_ = null;
                        if (object21 != null && object21 != Boolean.FALSE) {
                            Object object22 = ch;
                            ch = null;
                            ((IFn)const__22.getRawRoot()).invoke(object22);
                        }
                        Boolean inst_19609 = Boolean.TRUE;
                        Object statearr_19631 = this.state_19615;
                        Boolean bl = inst_19609;
                        inst_19609 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19631, 2L, (Object)bl);
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19631, 1L, const__5);
                        object2 = const__12;
                        break;
                    }
                    case 6: {
                        Object inst_19611 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 2L);
                        Object statearr_19632 = this.state_19615;
                        Object object23 = inst_19611;
                        inst_19611 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19632, 2L, object23);
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19632, 1L, const__1);
                        object2 = const__12;
                        break;
                    }
                    case 7: {
                        Object inst_19600;
                        Object object24 = inst_19600 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 2L);
                        inst_19600 = null;
                        if (object24 != null && object24 != Boolean.FALSE) {
                            Object statearr_19633 = this.state_19615;
                            ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19633, 1L, const__7);
                        } else {
                            Object statearr_19634 = this.state_19615;
                            ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19634, 1L, const__10);
                        }
                        object2 = const__12;
                        break;
                    }
                    case 8: {
                        Object inst_19602;
                        Object inst_19595;
                        Object object25 = inst_19595 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 9L);
                        inst_19595 = null;
                        Object object26 = inst_19602 = ((IFn)const__26.getRawRoot()).invoke(object25);
                        inst_19602 = null;
                        Object inst_195952 = object26;
                        Object statearr_19635 = this.state_19615;
                        Object object27 = inst_195952;
                        inst_195952 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19635, 9L, object27);
                        Object state_19615 = null;
                        Object object28 = state_19615 = statearr_19635;
                        state_19615 = null;
                        Object statearr_19636 = object28;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19636, 2L, null);
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19636, 1L, const__11);
                        object2 = const__12;
                        break;
                    }
                    case 9: {
                        Object object29;
                        Object inst_19591 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 6L);
                        Object inst_19592 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 7L);
                        Object inst_19593 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 8L);
                        Object inst_19595 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 9L);
                        Object object30 = inst_19591;
                        inst_19591 = null;
                        Object ch = object30;
                        inst_19592 = null;
                        Object object31 = inst_19593;
                        inst_19593 = null;
                        Object close_QMARK_ = object31;
                        inst_19595 = null;
                        Object object32 = close_QMARK_;
                        close_QMARK_ = null;
                        if (object32 != null && object32 != Boolean.FALSE) {
                            Object object33 = ch;
                            ch = null;
                            object29 = ((IFn)const__22.getRawRoot()).invoke(object33);
                        } else {
                            object29 = null;
                        }
                        Object inst_19605 = object29;
                        Object statearr_19637 = this.state_19615;
                        Object object34 = inst_19605;
                        inst_19605 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19637, 2L, object34);
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19637, 1L, const__28);
                        object2 = const__12;
                        break;
                    }
                    case 10: {
                        Object inst_19607 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 2L);
                        Object statearr_19638 = this.state_19615;
                        Object object35 = inst_19607;
                        inst_19607 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19638, 2L, object35);
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19638, 1L, const__5);
                        object2 = const__12;
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)const__30.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__19626));
                    }
                }
                result__9978__auto__19642 = object2;
            } while (Util.identical((Object)result__9978__auto__19642, (Object)const__12));
            Object object36 = result__9978__auto__19642;
            result__9978__auto__19642 = null;
            object = object36;
        }
        catch (Throwable ex__9979__auto__2) {
            Object statearr_19639 = this.state_19615;
            ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19639, 2L, (Object)ex__9979__auto__2);
            Object object37 = ((IFn)const__8.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 4L));
            if (object37 == null || object37 == Boolean.FALSE) {
                Object ex__9979__auto__2 = null;
                throw ex__9979__auto__2;
            }
            Object statearr_19640 = this.state_19615;
            ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19640, 1L, ((IFn)const__19.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19615, 4L)));
            object = const__12;
        }
        finally {
            this.state_19615 = null;
            ((IFn.OLOO)const__9.getRawRoot()).invokePrim(this.state_19615, 3L, Var.getThreadBindingFrame());
            this.old_frame__9976__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__9976__auto__);
        }
        return object;
    }
}

