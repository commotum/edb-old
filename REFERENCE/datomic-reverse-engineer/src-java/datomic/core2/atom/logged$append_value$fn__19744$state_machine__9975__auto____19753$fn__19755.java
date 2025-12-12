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
package datomic.core2.atom;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class logged$append_value$fn__19744$state_machine__9975__auto____19753$fn__19755
extends AFunction {
    Object state_19743;
    Object old_frame__9976__auto__;
    Object G__19717;
    Object G__19719;
    Object G__19718;
    Object G__19716;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
    public static final Object const__1 = 3L;
    public static final Object const__5 = 6L;
    public static final Var const__9 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__10 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
    public static final Object const__11 = 2L;
    public static final Var const__15 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
    public static final Object const__16 = 4L;
    public static final Keyword const__17 = RT.keyword(null, (String)"recur");
    public static final Object const__19 = 5L;
    public static final Var const__21 = RT.var((String)"datomic.core2.log", (String)"append");
    public static final Var const__23 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"first");

    public logged$append_value$fn__19744$state_machine__9975__auto____19753$fn__19755(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.state_19743 = object;
        this.old_frame__9976__auto__ = object2;
        this.G__19717 = object3;
        this.G__19719 = object4;
        this.G__19718 = object5;
        this.G__19716 = object6;
    }

    public Object invoke() {
        Object object;
        try {
            Object result__9978__auto__19767;
            Var.resetThreadBindingFrame((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 3L));
            do {
                Object object2;
                int G__19756 = RT.intCast((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 1L));
                switch (G__19756) {
                    case 1: {
                        Object state_19743;
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 6L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 7L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 8L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 9L);
                        Object inst_19721 = ((IFn)this.G__19716).invoke();
                        Object inst_19722 = ((IFn)this.G__19717).invoke();
                        Object inst_19723 = ((IFn)this.G__19718).invoke();
                        Object inst_19724 = ((IFn)this.G__19719).invoke();
                        Object object3 = inst_19721;
                        inst_19721 = null;
                        Object inst_19725 = object3;
                        Object object4 = inst_19722;
                        inst_19722 = null;
                        Object inst_19726 = object4;
                        Object object5 = inst_19723;
                        inst_19723 = null;
                        Object inst_19727 = object5;
                        Object object6 = inst_19724;
                        inst_19724 = null;
                        Object inst_19728 = object6;
                        Object serialize = inst_19726;
                        Object header = inst_19727;
                        Object value = inst_19728;
                        Object object7 = serialize;
                        serialize = null;
                        Object object8 = header;
                        header = null;
                        Object object9 = value;
                        value = null;
                        Object inst_19729 = ((IFn)object7).invoke(object8, object9);
                        Object statearr_19757 = this.state_19743;
                        Object object10 = inst_19725;
                        inst_19725 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19757, 6L, object10);
                        Object object11 = inst_19726;
                        inst_19726 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19757, 7L, object11);
                        Object object12 = inst_19727;
                        inst_19727 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19757, 8L, object12);
                        Object object13 = inst_19728;
                        inst_19728 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19757, 9L, object13);
                        Object object14 = statearr_19757;
                        statearr_19757 = null;
                        Object object15 = state_19743 = object14;
                        state_19743 = null;
                        Object object16 = inst_19729;
                        inst_19729 = null;
                        object2 = ((IFn)const__10.getRawRoot()).invoke(object15, const__11, object16);
                        break;
                    }
                    case 2: {
                        Object inst_19733;
                        Object inst_19732;
                        Object inst_19731;
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 10L);
                        Object inst_19725 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 6L);
                        Object inst_19726 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 7L);
                        Object inst_19728 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 9L);
                        Object inst_19727 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 8L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 11L);
                        Object object17 = inst_19731 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 2L);
                        inst_19731 = null;
                        Object body = inst_19732 = object17;
                        inst_19725 = null;
                        inst_19726 = null;
                        inst_19728 = null;
                        inst_19727 = null;
                        Object object18 = body;
                        body = null;
                        Object object19 = inst_19733 = ((IFn)const__15.getRawRoot()).invoke(object18);
                        inst_19733 = null;
                        Object inst_19734 = object19;
                        Object statearr_19758 = this.state_19743;
                        Object object20 = inst_19732;
                        inst_19732 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19758, 10L, object20);
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19758, 11L, inst_19734);
                        Object object21 = statearr_19758;
                        statearr_19758 = null;
                        Object state_19743 = object21;
                        Object object22 = inst_19734;
                        inst_19734 = null;
                        if (object22 != null && object22 != Boolean.FALSE) {
                            Object object23 = state_19743;
                            state_19743 = null;
                            Object statearr_19759 = object23;
                            ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19759, 1L, const__1);
                        } else {
                            Object object24 = state_19743;
                            state_19743 = null;
                            Object statearr_19760 = object24;
                            ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19760, 1L, const__16);
                        }
                        object2 = const__17;
                        break;
                    }
                    case 3: {
                        Object inst_19734 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 11L);
                        Object statearr_19761 = this.state_19743;
                        Object object25 = inst_19734;
                        inst_19734 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19761, 2L, object25);
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19761, 1L, const__19);
                        object2 = const__17;
                        break;
                    }
                    case 4: {
                        Object inst_19737;
                        Object inst_19732 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 10L);
                        Object inst_19725 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 6L);
                        Object inst_19726 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 7L);
                        Object inst_19734 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 11L);
                        Object inst_19728 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 9L);
                        Object inst_19727 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 8L);
                        Object object26 = inst_19732;
                        inst_19732 = null;
                        Object body = object26;
                        Object object27 = inst_19725;
                        inst_19725 = null;
                        Object log2 = object27;
                        inst_19726 = null;
                        inst_19734 = null;
                        inst_19728 = null;
                        Object object28 = inst_19727;
                        inst_19727 = null;
                        Object header = object28;
                        Object object29 = log2;
                        log2 = null;
                        Object object30 = header;
                        header = null;
                        Object object31 = body;
                        body = null;
                        Object object32 = inst_19737 = ((IFn)const__21.getRawRoot()).invoke(object29, object30, object31);
                        inst_19737 = null;
                        object2 = ((IFn)const__10.getRawRoot()).invoke(this.state_19743, const__5, object32);
                        break;
                    }
                    case 5: {
                        Object inst_19741;
                        Object object33 = inst_19741 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 2L);
                        inst_19741 = null;
                        object2 = ((IFn)const__23.getRawRoot()).invoke(this.state_19743, object33);
                        break;
                    }
                    case 6: {
                        Object inst_19739 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 2L);
                        Object statearr_19762 = this.state_19743;
                        Object object34 = inst_19739;
                        inst_19739 = null;
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19762, 2L, object34);
                        ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19762, 1L, const__19);
                        object2 = const__17;
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)const__25.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__19756));
                    }
                }
                result__9978__auto__19767 = object2;
            } while (Util.identical((Object)result__9978__auto__19767, (Object)const__17));
            Object object35 = result__9978__auto__19767;
            result__9978__auto__19767 = null;
            object = object35;
        }
        catch (Throwable ex__9979__auto__2) {
            Object statearr_19763 = this.state_19743;
            ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19763, 2L, (Object)ex__9979__auto__2);
            Object object36 = ((IFn)const__27.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 4L));
            if (object36 == null || object36 == Boolean.FALSE) {
                Object ex__9979__auto__2 = null;
                throw ex__9979__auto__2;
            }
            Object statearr_19764 = this.state_19743;
            ((IFn.OLOO)const__9.getRawRoot()).invokePrim(statearr_19764, 1L, ((IFn)const__28.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_19743, 4L)));
            object = const__17;
        }
        finally {
            this.state_19743 = null;
            ((IFn.OLOO)const__9.getRawRoot()).invokePrim(this.state_19743, 3L, Var.getThreadBindingFrame());
            this.old_frame__9976__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__9976__auto__);
        }
        return object;
    }
}

