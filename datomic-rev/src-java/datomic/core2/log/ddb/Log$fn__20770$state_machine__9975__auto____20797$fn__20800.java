/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2.log.ddb;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class Log$fn__20770$state_machine__9975__auto____20797$fn__20800
extends AFunction {
    Object G__20652;
    Object G__20663;
    Object G__20657;
    Object G__20659;
    Object G__20662;
    Object G__20653;
    Object G__20661;
    Object G__20658;
    Object old_frame__9976__auto__;
    Object G__20651;
    Object G__20655;
    Object state_20769;
    Object G__20660;
    Object G__20654;
    Object G__20656;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
    public static final Object const__1 = 3L;
    public static final Object const__3 = 1L;
    public static final Object const__5 = 14L;
    public static final Object const__6 = 15L;
    public static final Object const__7 = 16L;
    public static final Object const__8 = 17L;
    public static final Object const__9 = 18L;
    public static final Object const__10 = 8L;
    public static final Object const__11 = 19L;
    public static final Object const__12 = 20L;
    public static final Object const__13 = 21L;
    public static final Object const__14 = 22L;
    public static final Object const__15 = 23L;
    public static final Object const__16 = 24L;
    public static final Object const__17 = 25L;
    public static final Keyword const__18 = RT.keyword(null, (String)"forward");
    public static final Var const__19 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Object const__20 = 2L;
    public static final Keyword const__21 = RT.keyword(null, (String)"recur");
    public static final Keyword const__24 = RT.keyword(null, (String)"backward");
    public static final Var const__25 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
    public static final Object const__26 = 5L;
    public static final Object const__28 = 4L;
    public static final Object const__30 = 7L;
    public static final Object const__31 = 6L;
    public static final Object const__32 = 26L;
    public static final Var const__35 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
    public static final Object const__36 = 9L;
    public static final Var const__38 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
    public static final Var const__40 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"put!");
    public static final Object const__41 = 11L;
    public static final Var const__44 = RT.var((String)"clojure.core", (String)"seq");
    public static final Object const__45 = 12L;
    public static final Object const__46 = 13L;
    public static final Var const__49 = RT.var((String)"clojure.core.async", (String)"close!");
    public static final Object const__50 = 28L;
    public static final Object const__51 = 10L;
    public static final Var const__53 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__54 = RT.var((String)"datomic.core2.log.ddb", (String)"ddb-item->log-item");
    public static final Var const__55 = RT.var((String)"datomic.core2.async", (String)"put-all!");
    public static final Object const__58 = 27L;
    public static final Var const__61 = RT.var((String)"clojure.core", (String)"not");
    public static final Object const__72 = 29L;
    public static final Object const__73 = 30L;
    public static final Object const__78 = 32L;
    public static final Object const__80 = 31L;
    public static final Var const__83 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__85 = RT.var((String)"clojure.core", (String)"first");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"Items"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"Items"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"Count"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public Log$fn__20770$state_machine__9975__auto____20797$fn__20800(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14, Object object15) {
        this.G__20652 = object;
        this.G__20663 = object2;
        this.G__20657 = object3;
        this.G__20659 = object4;
        this.G__20662 = object5;
        this.G__20653 = object6;
        this.G__20661 = object7;
        this.G__20658 = object8;
        this.old_frame__9976__auto__ = object9;
        this.G__20651 = object10;
        this.G__20655 = object11;
        this.state_20769 = object12;
        this.G__20660 = object13;
        this.G__20654 = object14;
        this.G__20656 = object15;
    }

    public Object invoke() {
        Object object;
        try {
            Object result__9978__auto__20847;
            Var.resetThreadBindingFrame((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 3L));
            do {
                Object object2;
                int G__20801 = RT.intCast((Object)((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 1L));
                switch (G__20801) {
                    case 1: {
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 14L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 15L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 16L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 17L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 18L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 8L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 19L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 20L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 21L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 22L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 23L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 24L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 25L);
                        Object inst_20666 = ((IFn)this.G__20651).invoke();
                        Object inst_20667 = ((IFn)this.G__20652).invoke();
                        Object inst_20668 = ((IFn)this.G__20653).invoke();
                        Object inst_20669 = ((IFn)this.G__20654).invoke();
                        Object inst_20670 = ((IFn)this.G__20655).invoke();
                        Object inst_20671 = ((IFn)this.G__20656).invoke();
                        Object inst_20672 = ((IFn)this.G__20657).invoke();
                        Object inst_20673 = ((IFn)this.G__20658).invoke();
                        Object inst_20674 = ((IFn)this.G__20659).invoke();
                        Object inst_20675 = ((IFn)this.G__20660).invoke();
                        Object inst_20676 = ((IFn)this.G__20661).invoke();
                        Object inst_20677 = ((IFn)this.G__20662).invoke();
                        Object inst_20678 = ((IFn)this.G__20663).invoke();
                        Object object3 = inst_20666;
                        inst_20666 = null;
                        Object inst_20679 = object3;
                        Object object4 = inst_20667;
                        inst_20667 = null;
                        Object inst_20680 = object4;
                        Object object5 = inst_20668;
                        inst_20668 = null;
                        Object inst_20681 = object5;
                        Object object6 = inst_20669;
                        inst_20669 = null;
                        Object inst_20682 = object6;
                        Object object7 = inst_20670;
                        inst_20670 = null;
                        Object inst_20683 = object7;
                        Object object8 = inst_20671;
                        inst_20671 = null;
                        Object inst_20684 = object8;
                        Object object9 = inst_20672;
                        inst_20672 = null;
                        Object inst_20685 = object9;
                        Object object10 = inst_20673;
                        inst_20673 = null;
                        Object inst_20686 = object10;
                        Object object11 = inst_20674;
                        inst_20674 = null;
                        Object inst_20687 = object11;
                        Object object12 = inst_20675;
                        inst_20675 = null;
                        Object inst_20688 = object12;
                        Object object13 = inst_20676;
                        inst_20676 = null;
                        Object inst_20689 = object13;
                        Object object14 = inst_20677;
                        inst_20677 = null;
                        Object inst_20690 = object14;
                        Object object15 = inst_20678;
                        inst_20678 = null;
                        Object inst_20691 = object15;
                        Object direction = inst_20682;
                        Object object16 = direction;
                        direction = null;
                        boolean inst_20692 = Util.equiv((Object)object16, (Object)const__18);
                        Object statearr_20802 = this.state_20769;
                        Object object17 = inst_20679;
                        inst_20679 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 14L, object17);
                        Object object18 = inst_20680;
                        inst_20680 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 15L, object18);
                        Object object19 = inst_20681;
                        inst_20681 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 16L, object19);
                        Object object20 = inst_20682;
                        inst_20682 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 17L, object20);
                        Object object21 = inst_20683;
                        inst_20683 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 18L, object21);
                        Object object22 = inst_20684;
                        inst_20684 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 8L, object22);
                        Object object23 = inst_20685;
                        inst_20685 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 19L, object23);
                        Object object24 = inst_20686;
                        inst_20686 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 20L, object24);
                        Object object25 = inst_20687;
                        inst_20687 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 21L, object25);
                        Object object26 = inst_20688;
                        inst_20688 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 22L, object26);
                        Object object27 = inst_20689;
                        inst_20689 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 23L, object27);
                        Object object28 = inst_20690;
                        inst_20690 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 24L, object28);
                        Object object29 = inst_20691;
                        inst_20691 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20802, 25L, object29);
                        Object object30 = statearr_20802;
                        statearr_20802 = null;
                        Object state_20769 = object30;
                        if (inst_20692) {
                            Object object31 = state_20769;
                            state_20769 = null;
                            Object statearr_20803 = object31;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20803, 1L, const__20);
                        } else {
                            Object object32 = state_20769;
                            state_20769 = null;
                            Object statearr_20804 = object32;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20804, 1L, const__1);
                        }
                        object2 = const__21;
                        break;
                    }
                    case 2: {
                        Object inst_20694;
                        Object inst_20679 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 14L);
                        Object inst_20680 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 15L);
                        Object inst_20681 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 16L);
                        Object inst_20682 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 17L);
                        Object inst_20683 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 18L);
                        Object inst_20684 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 8L);
                        Object inst_20685 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 19L);
                        Object inst_20686 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 20L);
                        Object inst_20687 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 21L);
                        Object inst_20688 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 22L);
                        Object inst_20689 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 23L);
                        Object inst_20690 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 24L);
                        Object inst_20691 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 25L);
                        Object object33 = inst_20679;
                        inst_20679 = null;
                        Object t = object33;
                        Object object34 = inst_20680;
                        inst_20680 = null;
                        Object query2 = object34;
                        inst_20681 = null;
                        Object object35 = inst_20682;
                        inst_20682 = null;
                        Object direction = object35;
                        inst_20683 = null;
                        inst_20684 = null;
                        inst_20685 = null;
                        inst_20686 = null;
                        inst_20687 = null;
                        inst_20688 = null;
                        inst_20689 = null;
                        inst_20690 = null;
                        inst_20691 = null;
                        Object object36 = query2;
                        query2 = null;
                        Object object37 = direction;
                        direction = null;
                        Object object38 = t;
                        t = null;
                        Object object39 = inst_20694 = ((IFn)object36).invoke((Object)(Util.equiv((Object)object37, (Object)const__24) ? Boolean.TRUE : Boolean.FALSE), object38, const__3);
                        inst_20694 = null;
                        object2 = ((IFn)const__25.getRawRoot()).invoke(this.state_20769, const__26, object39);
                        break;
                    }
                    case 3: {
                        Object statearr_20805 = this.state_20769;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20805, 2L, null);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20805, 1L, const__28);
                        object2 = const__21;
                        break;
                    }
                    case 4: {
                        Object state_20769;
                        Object inst_20686 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 20L);
                        Object inst_20699 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        Object object40 = inst_20686;
                        inst_20686 = null;
                        Object inst_20700 = object40;
                        Object object41 = inst_20699;
                        inst_20699 = null;
                        Object inst_20701 = object41;
                        Boolean inst_20702 = Boolean.TRUE;
                        Object statearr_20806 = this.state_20769;
                        Object object42 = inst_20700;
                        inst_20700 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20806, 7L, object42);
                        Object object43 = inst_20701;
                        inst_20701 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20806, 6L, object43);
                        Boolean bl = inst_20702;
                        inst_20702 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20806, 26L, (Object)bl);
                        Object object44 = statearr_20806;
                        statearr_20806 = null;
                        Object object45 = state_20769 = object44;
                        state_20769 = null;
                        Object statearr_20807 = object45;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20807, 2L, null);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20807, 1L, const__31);
                        object2 = const__21;
                        break;
                    }
                    case 5: {
                        Object inst_20696 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        Object statearr_20808 = this.state_20769;
                        Object object46 = inst_20696;
                        inst_20696 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20808, 2L, object46);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20808, 1L, const__28);
                        object2 = const__21;
                        break;
                    }
                    case 6: {
                        Object inst_20704;
                        Object inst_20701;
                        Object object47 = inst_20701 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 6L);
                        inst_20701 = null;
                        Object object48 = inst_20704 = ((IFn)const__35.getRawRoot()).invoke(object47);
                        inst_20704 = null;
                        if (object48 != null && object48 != Boolean.FALSE) {
                            Object statearr_20809 = this.state_20769;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20809, 1L, const__10);
                        } else {
                            Object statearr_20810 = this.state_20769;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20810, 1L, const__36);
                        }
                        object2 = const__21;
                        break;
                    }
                    case 7: {
                        Object inst_20767;
                        Object object49 = inst_20767 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        inst_20767 = null;
                        object2 = ((IFn)const__38.getRawRoot()).invoke(this.state_20769, object49);
                        break;
                    }
                    case 8: {
                        Object inst_20689 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 23L);
                        Object inst_20701 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 6L);
                        Object object50 = inst_20689;
                        inst_20689 = null;
                        Object object51 = inst_20701;
                        inst_20701 = null;
                        object2 = ((IFn)const__40.getRawRoot()).invoke(this.state_20769, const__41, object50, object51);
                        break;
                    }
                    case 9: {
                        Object inst_20711;
                        Object inst_20710;
                        Object inst_20701 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 6L);
                        ILookupThunk iLookupThunk = __thunk__0__;
                        Object object52 = inst_20701;
                        inst_20701 = null;
                        Object object53 = iLookupThunk.get(object52);
                        if (iLookupThunk == object53) {
                            __thunk__0__ = __site__0__.fault(object52);
                            object53 = __thunk__0__.get(object52);
                        }
                        Object object54 = inst_20710 = object53;
                        inst_20710 = null;
                        Object object55 = inst_20711 = ((IFn)const__44.getRawRoot()).invoke(object54);
                        inst_20711 = null;
                        if (object55 != null && object55 != Boolean.FALSE) {
                            Object statearr_20811 = this.state_20769;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20811, 1L, const__45);
                        } else {
                            Object statearr_20812 = this.state_20769;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20812, 1L, const__46);
                        }
                        object2 = const__21;
                        break;
                    }
                    case 10: {
                        Object inst_20765 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        Object statearr_20813 = this.state_20769;
                        Object object56 = inst_20765;
                        inst_20765 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20813, 2L, object56);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20813, 1L, const__30);
                        object2 = const__21;
                        break;
                    }
                    case 11: {
                        Object state_20769;
                        Object inst_20679 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 14L);
                        Object inst_20680 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 15L);
                        Object inst_20681 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 16L);
                        Object inst_20702 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 26L);
                        Object inst_20682 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 17L);
                        Object inst_20683 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 18L);
                        Object inst_20684 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 8L);
                        Object inst_20685 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 19L);
                        Object inst_20700 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 7L);
                        Object inst_20687 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 21L);
                        Object inst_20701 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 6L);
                        Object inst_20688 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 22L);
                        Object inst_20689 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 23L);
                        Object inst_20690 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 24L);
                        Object inst_20691 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 25L);
                        Object inst_20707 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        inst_20679 = null;
                        inst_20680 = null;
                        inst_20681 = null;
                        inst_20702 = null;
                        inst_20682 = null;
                        inst_20683 = null;
                        inst_20684 = null;
                        inst_20685 = null;
                        inst_20700 = null;
                        inst_20687 = null;
                        inst_20701 = null;
                        inst_20688 = null;
                        Object object57 = inst_20689;
                        inst_20689 = null;
                        Object ch = object57;
                        inst_20690 = null;
                        inst_20691 = null;
                        Object object58 = ch;
                        ch = null;
                        Object inst_20708 = ((IFn)const__49.getRawRoot()).invoke(object58);
                        Object statearr_20814 = this.state_20769;
                        Object object59 = inst_20707;
                        inst_20707 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20814, 28L, object59);
                        Object object60 = statearr_20814;
                        statearr_20814 = null;
                        Object object61 = state_20769 = object60;
                        state_20769 = null;
                        Object statearr_20815 = object61;
                        Object object62 = inst_20708;
                        inst_20708 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20815, 2L, object62);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20815, 1L, const__51);
                        object2 = const__21;
                        break;
                    }
                    case 12: {
                        Object inst_20715;
                        Object inst_20713;
                        Object inst_20701 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 6L);
                        Object inst_20689 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 23L);
                        ILookupThunk iLookupThunk = __thunk__1__;
                        Object object63 = inst_20701;
                        inst_20701 = null;
                        Object object64 = iLookupThunk.get(object63);
                        if (iLookupThunk == object64) {
                            __thunk__1__ = __site__1__.fault(object63);
                            object64 = __thunk__1__.get(object63);
                        }
                        Object object65 = inst_20713 = object64;
                        inst_20713 = null;
                        Object inst_20714 = ((IFn)const__53.getRawRoot()).invoke(const__54.getRawRoot(), object65);
                        Object object66 = inst_20689;
                        inst_20689 = null;
                        Object object67 = inst_20714;
                        inst_20714 = null;
                        Object object68 = inst_20715 = ((IFn)const__55.getRawRoot()).invoke(object66, object67, (Object)Boolean.FALSE);
                        inst_20715 = null;
                        object2 = ((IFn)const__25.getRawRoot()).invoke(this.state_20769, const__6, object68);
                        break;
                    }
                    case 13: {
                        Object statearr_20816 = this.state_20769;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20816, 2L, null);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20816, 1L, const__5);
                        object2 = const__21;
                        break;
                    }
                    case 14: {
                        Object inst_20720;
                        Object inst_20702 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 26L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 27L);
                        Object object69 = inst_20720 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        inst_20720 = null;
                        Object inst_20721 = object69;
                        Object object70 = inst_20702;
                        inst_20702 = null;
                        Object inst_20722 = object70;
                        Object statearr_20817 = this.state_20769;
                        Object object71 = inst_20721;
                        inst_20721 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20817, 9L, object71);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20817, 27L, inst_20722);
                        Object object72 = statearr_20817;
                        statearr_20817 = null;
                        Object state_20769 = object72;
                        Object object73 = inst_20722;
                        inst_20722 = null;
                        if (object73 != null && object73 != Boolean.FALSE) {
                            Object object74 = state_20769;
                            state_20769 = null;
                            Object statearr_20818 = object74;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20818, 1L, const__7);
                        } else {
                            Object object75 = state_20769;
                            state_20769 = null;
                            Object statearr_20819 = object75;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20819, 1L, const__8);
                        }
                        object2 = const__21;
                        break;
                    }
                    case 15: {
                        Object inst_20717 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        Object statearr_20820 = this.state_20769;
                        Object object76 = inst_20717;
                        inst_20717 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20820, 2L, object76);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20820, 1L, const__5);
                        object2 = const__21;
                        break;
                    }
                    case 16: {
                        Object inst_20721;
                        Object object77 = inst_20721 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 9L);
                        inst_20721 = null;
                        Object inst_20724 = ((IFn)const__61.getRawRoot()).invoke(object77);
                        Object statearr_20821 = this.state_20769;
                        Object object78 = inst_20724;
                        inst_20724 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20821, 2L, object78);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20821, 1L, const__9);
                        object2 = const__21;
                        break;
                    }
                    case 17: {
                        Object inst_20722 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 27L);
                        Object statearr_20822 = this.state_20769;
                        Object object79 = inst_20722;
                        inst_20722 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20822, 2L, object79);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20822, 1L, const__9);
                        object2 = const__21;
                        break;
                    }
                    case 18: {
                        Object inst_20727;
                        Object object80 = inst_20727 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        inst_20727 = null;
                        if (object80 != null && object80 != Boolean.FALSE) {
                            Object statearr_20823 = this.state_20769;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20823, 1L, const__11);
                        } else {
                            Object statearr_20824 = this.state_20769;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20824, 1L, const__12);
                        }
                        object2 = const__21;
                        break;
                    }
                    case 19: {
                        Object inst_20730;
                        Object inst_20682 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 17L);
                        Object inst_20680 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 15L);
                        Object inst_20679 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 14L);
                        Object inst_20700 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 7L);
                        Object object81 = inst_20682;
                        inst_20682 = null;
                        boolean inst_20729 = Util.equiv((Object)object81, (Object)const__18);
                        Object object82 = inst_20680;
                        inst_20680 = null;
                        Object object83 = inst_20679;
                        inst_20679 = null;
                        Object object84 = inst_20700;
                        inst_20700 = null;
                        Object object85 = inst_20730 = ((IFn)object82).invoke((Object)(inst_20729 ? Boolean.TRUE : Boolean.FALSE), object83, object84);
                        inst_20730 = null;
                        object2 = ((IFn)const__25.getRawRoot()).invoke(this.state_20769, const__14, object85);
                        break;
                    }
                    case 20: {
                        Object inst_20701 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 6L);
                        Object inst_20700 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 7L);
                        Object inst_20684 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 8L);
                        Object inst_20721 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 9L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 10L);
                        ILookupThunk iLookupThunk = __thunk__2__;
                        Object object86 = inst_20701;
                        Object object87 = iLookupThunk.get(object86);
                        if (iLookupThunk == object87) {
                            __thunk__2__ = __site__2__.fault(object86);
                            object87 = __thunk__2__.get(object86);
                        }
                        Object inst_20735 = object87;
                        Object object88 = inst_20700;
                        inst_20700 = null;
                        Object object89 = inst_20735;
                        inst_20735 = null;
                        Number inst_20736 = Numbers.minus((Object)object88, (Object)object89);
                        Object object90 = inst_20684;
                        inst_20684 = null;
                        Object object91 = inst_20701;
                        inst_20701 = null;
                        Object inst_20737 = ((IFn)object90).invoke(object91);
                        Number number = inst_20736;
                        inst_20736 = null;
                        Number inst_20738 = number;
                        Object object92 = inst_20737;
                        inst_20737 = null;
                        Object inst_20739 = object92;
                        Object object93 = inst_20721;
                        inst_20721 = null;
                        Object inst_20740 = object93;
                        Object statearr_20825 = this.state_20769;
                        Number number2 = inst_20738;
                        inst_20738 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20825, 11L, (Object)number2);
                        Object object94 = inst_20739;
                        inst_20739 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20825, 12L, object94);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20825, 10L, inst_20740);
                        Object object95 = statearr_20825;
                        statearr_20825 = null;
                        Object state_20769 = object95;
                        Object object96 = inst_20740;
                        inst_20740 = null;
                        if (object96 != null && object96 != Boolean.FALSE) {
                            Object object97 = state_20769;
                            state_20769 = null;
                            Object statearr_20826 = object97;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20826, 1L, const__15);
                        } else {
                            Object object98 = state_20769;
                            state_20769 = null;
                            Object statearr_20827 = object98;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20827, 1L, const__16);
                        }
                        object2 = const__21;
                        break;
                    }
                    case 21: {
                        Object inst_20763 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        Object statearr_20828 = this.state_20769;
                        Object object99 = inst_20763;
                        inst_20763 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20828, 2L, object99);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20828, 1L, const__51);
                        object2 = const__21;
                        break;
                    }
                    case 22: {
                        Object state_20769;
                        Object tmp20799;
                        Object inst_20700 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 7L);
                        Object inst_20732 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        Object object100 = inst_20700;
                        inst_20700 = null;
                        Object object101 = tmp20799 = object100;
                        tmp20799 = null;
                        Object inst_207002 = object101;
                        Object object102 = inst_20732;
                        inst_20732 = null;
                        Object inst_20701 = object102;
                        Boolean inst_20702 = Boolean.FALSE;
                        Object statearr_20829 = this.state_20769;
                        Object object103 = inst_207002;
                        inst_207002 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20829, 7L, object103);
                        Object object104 = inst_20701;
                        inst_20701 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20829, 6L, object104);
                        Boolean bl = inst_20702;
                        inst_20702 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20829, 26L, (Object)bl);
                        Object object105 = statearr_20829;
                        statearr_20829 = null;
                        Object object106 = state_20769 = object105;
                        state_20769 = null;
                        Object statearr_20830 = object106;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20830, 2L, null);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20830, 1L, const__31);
                        object2 = const__21;
                        break;
                    }
                    case 23: {
                        boolean inst_20742;
                        Object inst_20738 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 11L);
                        ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 13L);
                        Object object107 = inst_20738;
                        inst_20738 = null;
                        boolean inst_20743 = inst_20742 = Numbers.isPos((Object)object107);
                        Object statearr_20831 = this.state_20769;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20831, 13L, (Object)(inst_20743 ? Boolean.TRUE : Boolean.FALSE));
                        Object object108 = statearr_20831;
                        statearr_20831 = null;
                        Object state_20769 = object108;
                        if (inst_20743) {
                            Object object109 = state_20769;
                            state_20769 = null;
                            Object statearr_20832 = object109;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20832, 1L, const__32);
                        } else {
                            Object object110 = state_20769;
                            state_20769 = null;
                            Object statearr_20833 = object110;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20833, 1L, const__58);
                        }
                        object2 = const__21;
                        break;
                    }
                    case 24: {
                        Object inst_20740 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 10L);
                        Object statearr_20834 = this.state_20769;
                        Object object111 = inst_20740;
                        inst_20740 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20834, 2L, object111);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20834, 1L, const__17);
                        object2 = const__21;
                        break;
                    }
                    case 25: {
                        Object inst_20750;
                        Object object112 = inst_20750 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        inst_20750 = null;
                        if (object112 != null && object112 != Boolean.FALSE) {
                            Object statearr_20835 = this.state_20769;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20835, 1L, const__72);
                        } else {
                            Object statearr_20836 = this.state_20769;
                            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20836, 1L, const__73);
                        }
                        object2 = const__21;
                        break;
                    }
                    case 26: {
                        Object inst_20739 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 12L);
                        Object statearr_20837 = this.state_20769;
                        Object object113 = inst_20739;
                        inst_20739 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20837, 2L, object113);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20837, 1L, const__50);
                        object2 = const__21;
                        break;
                    }
                    case 27: {
                        Object inst_20743 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 13L);
                        Object statearr_20838 = this.state_20769;
                        Object object114 = inst_20743;
                        inst_20743 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20838, 2L, object114);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20838, 1L, const__50);
                        object2 = const__21;
                        break;
                    }
                    case 28: {
                        Object inst_20747 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        Object statearr_20839 = this.state_20769;
                        Object object115 = inst_20747;
                        inst_20747 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20839, 2L, object115);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20839, 1L, const__17);
                        object2 = const__21;
                        break;
                    }
                    case 29: {
                        Object inst_20754;
                        Object inst_20682 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 17L);
                        Object inst_20684 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 8L);
                        Object inst_20701 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 6L);
                        Object inst_20680 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 15L);
                        Object inst_20738 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 11L);
                        Object object116 = inst_20682;
                        inst_20682 = null;
                        boolean inst_20752 = Util.equiv((Object)object116, (Object)const__18);
                        Object object117 = inst_20684;
                        inst_20684 = null;
                        Object object118 = inst_20701;
                        inst_20701 = null;
                        Object inst_20753 = ((IFn)object117).invoke(object118);
                        Object object119 = inst_20680;
                        inst_20680 = null;
                        Object object120 = inst_20753;
                        inst_20753 = null;
                        Object object121 = inst_20738;
                        inst_20738 = null;
                        Object object122 = inst_20754 = ((IFn)object119).invoke((Object)(inst_20752 ? Boolean.TRUE : Boolean.FALSE), object120, object121);
                        inst_20754 = null;
                        object2 = ((IFn)const__25.getRawRoot()).invoke(this.state_20769, const__78, object122);
                        break;
                    }
                    case 30: {
                        Object inst_20679 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 14L);
                        Object inst_20680 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 15L);
                        Object inst_20681 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 16L);
                        Object inst_20702 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 26L);
                        Object inst_20682 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 17L);
                        Object inst_20683 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 18L);
                        Object inst_20739 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 12L);
                        Object inst_20684 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 8L);
                        Object inst_20685 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 19L);
                        Object inst_20738 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 11L);
                        Object inst_20687 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 21L);
                        Object inst_20701 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 6L);
                        Object inst_20688 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 22L);
                        Object inst_20689 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 23L);
                        Object inst_20690 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 24L);
                        Object inst_20721 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 9L);
                        Object inst_20691 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 25L);
                        inst_20679 = null;
                        inst_20680 = null;
                        inst_20681 = null;
                        inst_20702 = null;
                        inst_20682 = null;
                        inst_20683 = null;
                        inst_20739 = null;
                        inst_20684 = null;
                        inst_20685 = null;
                        inst_20738 = null;
                        inst_20687 = null;
                        inst_20701 = null;
                        inst_20688 = null;
                        Object object123 = inst_20689;
                        inst_20689 = null;
                        Object ch = object123;
                        inst_20690 = null;
                        inst_20721 = null;
                        inst_20691 = null;
                        Object object124 = ch;
                        ch = null;
                        Object inst_20759 = ((IFn)const__49.getRawRoot()).invoke(object124);
                        Object statearr_20840 = this.state_20769;
                        Object object125 = inst_20759;
                        inst_20759 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20840, 2L, object125);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20840, 1L, const__80);
                        object2 = const__21;
                        break;
                    }
                    case 31: {
                        Object inst_20761 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        Object statearr_20841 = this.state_20769;
                        Object object126 = inst_20761;
                        inst_20761 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20841, 2L, object126);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20841, 1L, const__13);
                        object2 = const__21;
                        break;
                    }
                    case 32: {
                        Object state_20769;
                        Object inst_20738 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 11L);
                        Object inst_20756 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 2L);
                        Object object127 = inst_20738;
                        inst_20738 = null;
                        Object inst_20700 = object127;
                        Object object128 = inst_20756;
                        inst_20756 = null;
                        Object inst_20701 = object128;
                        Boolean inst_20702 = Boolean.FALSE;
                        Object statearr_20842 = this.state_20769;
                        Object object129 = inst_20700;
                        inst_20700 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20842, 7L, object129);
                        Object object130 = inst_20701;
                        inst_20701 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20842, 6L, object130);
                        Boolean bl = inst_20702;
                        inst_20702 = null;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20842, 26L, (Object)bl);
                        Object object131 = statearr_20842;
                        statearr_20842 = null;
                        Object object132 = state_20769 = object131;
                        state_20769 = null;
                        Object statearr_20843 = object132;
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20843, 2L, null);
                        ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20843, 1L, const__31);
                        object2 = const__21;
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)const__83.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__20801));
                    }
                }
                result__9978__auto__20847 = object2;
            } while (Util.identical((Object)result__9978__auto__20847, (Object)const__21));
            Object object133 = result__9978__auto__20847;
            result__9978__auto__20847 = null;
            object = object133;
        }
        catch (Throwable ex__9979__auto__2) {
            Object statearr_20844 = this.state_20769;
            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20844, 2L, (Object)ex__9979__auto__2);
            Object object134 = ((IFn)const__44.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 4L));
            if (object134 == null || object134 == Boolean.FALSE) {
                Object ex__9979__auto__2 = null;
                throw ex__9979__auto__2;
            }
            Object statearr_20845 = this.state_20769;
            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(statearr_20845, 1L, ((IFn)const__85.getRawRoot()).invoke(((IFn.OLO)const__0.getRawRoot()).invokePrim(this.state_20769, 4L)));
            object = const__21;
        }
        finally {
            this.state_20769 = null;
            ((IFn.OLOO)const__19.getRawRoot()).invokePrim(this.state_20769, 3L, Var.getThreadBindingFrame());
            this.old_frame__9976__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__9976__auto__);
        }
        return object;
    }
}

