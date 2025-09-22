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
package datomic.garbage;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.garbage.pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700$fn__22703;
import datomic.process.CriticalFailure;

public final class pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700
extends AFunction {
    Object state_22692;
    Object G__22601;
    Object old_frame__6361__auto__;
    Object G__22600;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Object const__1;
    public static final Object const__3;
    public static final Object const__5;
    public static final Object const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Object const__12;
    public static final Object const__14;
    public static final Object const__15;
    public static final Object const__16;
    public static final Var const__17;
    public static final Keyword const__18;
    public static final Var const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final Object const__27;
    public static final Object const__29;
    public static final Object const__32;
    public static final Keyword const__37;
    public static final Object const__39;
    public static final Object const__40;
    public static final Object const__42;
    public static final Object const__43;
    public static final Object const__44;
    public static final Object const__46;
    public static final Var const__48;
    public static final Var const__50;
    public static final Var const__51;
    public static final Keyword const__53;
    public static final Object const__55;
    public static final Var const__60;
    public static final Keyword const__65;
    public static final Var const__68;
    public static final Var const__69;
    public static final Var const__75;

    public pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700(Object object, Object object2, Object object3, Object object4) {
        this.state_22692 = object;
        this.G__22601 = object2;
        this.old_frame__6361__auto__ = object3;
        this.G__22600 = object4;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            Var.resetThreadBindingFrame((Object)((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 3L));
            do {
                G__22701 = RT.intCast((Object)((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 1L));
                switch (G__22701) {
                    case 1: {
                        ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 6L);
                        ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 7L);
                        inst_22618 = ((IFn)this.G__22600).invoke();
                        inst_22619 = ((IFn)this.G__22601).invoke();
                        v0 = inst_22618;
                        inst_22618 = null;
                        inst_22620 = v0;
                        v1 = inst_22619;
                        inst_22619 = null;
                        inst_22621 = v1;
                        inst_22622 = ((IFn.LO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__7.getRawRoot()).invokePrim(RT.longCast((Object)((Number)((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__8.getRawRoot()).invoke(pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__9.getRawRoot()))));
                        statearr_22702 = this.state_22692;
                        v2 = inst_22620;
                        inst_22620 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22702, 6L, v2);
                        v3 = inst_22621;
                        inst_22621 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22702, 7L, v3);
                        v4 = statearr_22702;
                        statearr_22702 = null;
                        v5 = state_22692 = v4;
                        state_22692 = null;
                        v6 = inst_22622;
                        inst_22622 = null;
                        v7 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__11.getRawRoot()).invoke(v5, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__12, v6);
                        break;
                    }
                    case 2: {
                        inst_22620 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 6L);
                        inst_22621 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 7L);
                        ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 10L);
                        ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 14L);
                        ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 15L);
                        inst_22624 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 2L);
                        v8 = garbage_ids_ref = inst_22621;
                        garbage_ids_ref = null;
                        inst_22625 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__8.getRawRoot()).invoke(v8);
                        v9 = garbage_ids = inst_22625;
                        garbage_ids = null;
                        inst_22626 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__17.getRawRoot()).invoke((Object)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__18, (Object)RT.count((Object)v9));
                        cluster = inst_22620;
                        garbage_ids = inst_22625;
                        v10 = cluster;
                        cluster = null;
                        v11 = garbage_ids;
                        garbage_ids = null;
                        inst_22627 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__20.getRawRoot()).invoke(((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__21.getRawRoot()).invoke(pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__22.getRawRoot(), (Object)new pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700$fn__22703(v10)), v11);
                        inst_22628 = ((IFn.LO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__7.getRawRoot()).invokePrim(RT.longCast((Object)((Number)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__23.getRawRoot())));
                        v12 = inst_22625;
                        inst_22625 = null;
                        inst_22629 = v12;
                        v13 = inst_22626;
                        inst_22626 = null;
                        inst_22630 = v13;
                        v14 = inst_22627;
                        inst_22627 = null;
                        inst_22631 = v14;
                        v15 = inst_22628;
                        inst_22628 = null;
                        inst_22632 = v15;
                        vec__22608 = inst_22631;
                        v16 = vec__22608;
                        vec__22608 = null;
                        inst_22633 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__24.getRawRoot()).invoke(v16);
                        seq__22609 = inst_22633;
                        v17 = seq__22609;
                        seq__22609 = null;
                        inst_22634 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__25.getRawRoot()).invoke(v17);
                        seq__22609 = inst_22633;
                        inst_22620 = null;
                        inst_22621 = null;
                        v18 = seq__22609;
                        seq__22609 = null;
                        inst_22635 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__26.getRawRoot()).invoke(v18);
                        inst_22636 = inst_22631;
                        v19 = inst_22631;
                        inst_22631 = null;
                        inst_22637 = v19;
                        v20 = inst_22633;
                        inst_22633 = null;
                        inst_22638 = v20;
                        inst_22639 = inst_22634;
                        inst_22640 = inst_22635;
                        v21 = inst_22634;
                        inst_22634 = null;
                        inst_22641 = v21;
                        v22 = inst_22635;
                        inst_22635 = null;
                        inst_22642 = v22;
                        inst_22643 = 0L;
                        v23 = inst_22636;
                        inst_22636 = null;
                        inst_22644 = v23;
                        inst_22645 = inst_22643;
                        statearr_22705 = this.state_22692;
                        v24 = inst_22624;
                        inst_22624 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22705, 24L, v24);
                        v25 = inst_22629;
                        inst_22629 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22705, 15L, v25);
                        v26 = inst_22630;
                        inst_22630 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22705, 14L, v26);
                        v27 = inst_22632;
                        inst_22632 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22705, 10L, v27);
                        v28 = inst_22637;
                        inst_22637 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22705, 8L, v28);
                        v29 = inst_22638;
                        inst_22638 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22705, 25L, v29);
                        v30 = inst_22639;
                        inst_22639 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22705, 18L, v30);
                        v31 = inst_22640;
                        inst_22640 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22705, 11L, v31);
                        v32 = inst_22641;
                        inst_22641 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22705, 26L, v32);
                        v33 = inst_22642;
                        inst_22642 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22705, 27L, v33);
                        v34 = inst_22644;
                        inst_22644 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22705, 19L, v34);
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22705, 21L, (Object)Numbers.num((long)inst_22645));
                        v35 = statearr_22705;
                        statearr_22705 = null;
                        v36 = state_22692 = v35;
                        state_22692 = null;
                        statearr_22706 = v36;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22706, 2L, null);
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22706, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__1);
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 3: {
                        inst_22644 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 19L);
                        inst_22645 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 21L);
                        ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 17L);
                        inst_22647 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__24.getRawRoot()).invoke(inst_22644);
                        inst_22648 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__25.getRawRoot()).invoke(inst_22647);
                        inst_22649 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__26.getRawRoot()).invoke(inst_22647);
                        v37 = inst_22644;
                        inst_22644 = null;
                        inst_22650 = v37;
                        v38 = inst_22647;
                        inst_22647 = null;
                        inst_22651 = v38;
                        inst_22652 = inst_22648;
                        inst_22653 = inst_22649;
                        v39 = inst_22648;
                        inst_22648 = null;
                        inst_22654 = v39;
                        v40 = inst_22649;
                        inst_22649 = null;
                        inst_22655 = v40;
                        v41 = inst_22645;
                        inst_22645 = null;
                        inst_22656 = v41;
                        statearr_22707 = this.state_22692;
                        v42 = inst_22650;
                        inst_22650 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22707, 12L, v42);
                        v43 = inst_22651;
                        inst_22651 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22707, 22L, v43);
                        v44 = inst_22652;
                        inst_22652 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22707, 9L, v44);
                        v45 = inst_22653;
                        inst_22653 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22707, 13L, v45);
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22707, 17L, inst_22654);
                        v46 = inst_22655;
                        inst_22655 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22707, 16L, v46);
                        v47 = inst_22656;
                        inst_22656 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22707, 20L, v47);
                        v48 = statearr_22707;
                        statearr_22707 = null;
                        state_22692 = v48;
                        v49 = inst_22654;
                        inst_22654 = null;
                        if (v49 != null && v49 != Boolean.FALSE) {
                            v50 = state_22692;
                            state_22692 = null;
                            statearr_22708 = v50;
                            ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22708, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__46);
                        } else {
                            v51 = state_22692;
                            state_22692 = null;
                            statearr_22709 = v51;
                            ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22709, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__5);
                        }
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 4: {
                        v52 = inst_22690 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 2L);
                        inst_22690 = null;
                        v7 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__48.getRawRoot()).invoke(this.state_22692, v52);
                        break;
                    }
                    case 5: {
                        inst_22654 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 17L);
                        inst_22632 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 10L);
                        v53 = inst_22654;
                        inst_22654 = null;
                        v54 = inst_22632;
                        inst_22632 = null;
                        v55 = inst_22658 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__50.getRawRoot()).invoke(v53, v54);
                        inst_22658 = null;
                        v7 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__51.getRawRoot()).invoke(this.state_22692, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__29, v55);
                        break;
                    }
                    case 6: {
                        inst_22637 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 8L);
                        inst_22652 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 9L);
                        inst_22632 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 10L);
                        inst_22640 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 11L);
                        inst_22650 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 12L);
                        inst_22653 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 13L);
                        inst_22620 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 6L);
                        inst_22630 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 14L);
                        inst_22629 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 15L);
                        inst_22655 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 16L);
                        inst_22621 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 7L);
                        inst_22654 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 17L);
                        inst_22639 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 18L);
                        inst_22644 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 19L);
                        inst_22656 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 20L);
                        inst_22637 = null;
                        inst_22652 = null;
                        inst_22632 = null;
                        inst_22640 = null;
                        inst_22650 = null;
                        inst_22653 = null;
                        inst_22620 = null;
                        inst_22630 = null;
                        inst_22629 = null;
                        inst_22655 = null;
                        inst_22621 = null;
                        inst_22654 = null;
                        inst_22639 = null;
                        inst_22644 = null;
                        v56 = inst_22656;
                        inst_22656 = null;
                        v57 = count = v56;
                        count = null;
                        inst_22686 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__17.getRawRoot()).invoke((Object)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__53, v57);
                        statearr_22710 = this.state_22692;
                        v58 = inst_22686;
                        inst_22686 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22710, 2L, v58);
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22710, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__6);
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 7: {
                        inst_22688 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 2L);
                        statearr_22711 = this.state_22692;
                        v59 = inst_22688;
                        inst_22688 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22711, 2L, v59);
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22711, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__55);
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 8: {
                        inst_22632 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 10L);
                        inst_22660 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 2L);
                        inst_22661 = RT.nth((Object)inst_22660, (int)RT.intCast((long)0L), null);
                        inst_22662 = RT.nth((Object)inst_22660, (int)RT.intCast((long)1L), null);
                        v60 = inst_22660;
                        inst_22660 = null;
                        inst_22663 = v60;
                        v61 = inst_22661;
                        inst_22661 = null;
                        inst_22664 = v61;
                        v62 = inst_22662;
                        inst_22662 = null;
                        v63 = inst_22665 = v62;
                        inst_22665 = null;
                        v64 = inst_22632;
                        inst_22632 = null;
                        inst_22666 = Util.equiv((Object)v63, (Object)v64);
                        statearr_22712 = this.state_22692;
                        v65 = inst_22663;
                        inst_22663 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22712, 29L, v65);
                        v66 = inst_22664;
                        inst_22664 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22712, 23L, v66);
                        v67 = statearr_22712;
                        statearr_22712 = null;
                        state_22692 = v67;
                        if (inst_22666) {
                            v68 = state_22692;
                            state_22692 = null;
                            statearr_22713 = v68;
                            ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22713, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__42);
                        } else {
                            v69 = state_22692;
                            state_22692 = null;
                            statearr_22714 = v69;
                            ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22714, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__14);
                        }
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 9: {
                        inst_22668 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__60.getRawRoot()).invoke((Object)"Cluster pod gc timed out");
                        statearr_22715 = this.state_22692;
                        v70 = inst_22668;
                        inst_22668 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22715, 2L, v70);
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22715, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__32);
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 10: {
                        v71 = inst_22664 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 23L);
                        inst_22664 = null;
                        inst_22670 = v71 instanceof Throwable;
                        if (inst_22670) {
                            statearr_22716 = this.state_22692;
                            ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22716, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__40);
                        } else {
                            statearr_22717 = this.state_22692;
                            ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22717, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__43);
                        }
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 11: {
                        inst_22664 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 23L);
                        inst_22677 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 2L);
                        v72 = inst_22664;
                        inst_22664 = null;
                        inst_22678 = Util.equiv((Object)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__65, (Object)v72);
                        statearr_22718 = this.state_22692;
                        v73 = inst_22677;
                        inst_22677 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22718, 28L, v73);
                        v74 = statearr_22718;
                        statearr_22718 = null;
                        state_22692 = v74;
                        if (inst_22678) {
                            v75 = state_22692;
                            state_22692 = null;
                            statearr_22719 = v75;
                            ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22719, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__16);
                        } else {
                            v76 = state_22692;
                            state_22692 = null;
                            statearr_22720 = v76;
                            ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22720, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__44);
                        }
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 12: {
                        inst_22664 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 23L);
                        v77 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__69.getRawRoot();
                        if (Util.classOf((Object)v77) == pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.__cached_class__0) ** GOTO lbl462
                        if (!(v77 instanceof CriticalFailure)) {
                            v77 = v77;
                            pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.__cached_class__0 = Util.classOf((Object)v77);
lbl462:
                            // 2 sources

                            v78 = inst_22664;
                            inst_22664 = null;
                            v79 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__68.getRawRoot().invoke(v77, (Object)"Cluster pod gc failed", v78);
                        } else {
                            v80 = inst_22664;
                            inst_22664 = null;
                            v79 = ((CriticalFailure)v77).fail("Cluster pod gc failed", v80);
                        }
                        inst_22672 = v79;
                        statearr_22721 = this.state_22692;
                        v81 = inst_22672;
                        inst_22672 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22721, 2L, v81);
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22721, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__15);
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 13: {
                        statearr_22722 = this.state_22692;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22722, 2L, null);
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22722, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__15);
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 14: {
                        inst_22675 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 2L);
                        statearr_22723 = this.state_22692;
                        v82 = inst_22675;
                        inst_22675 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22723, 2L, v82);
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22723, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__32);
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 15: {
                        statearr_22724 = this.state_22692;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22724, 2L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__3);
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22724, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__39);
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 16: {
                        statearr_22725 = this.state_22692;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22725, 2L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__27);
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22725, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__39);
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    case 17: {
                        inst_22656 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 20L);
                        inst_22655 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 16L);
                        inst_22682 = ((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 2L);
                        v83 = inst_22656;
                        inst_22656 = null;
                        v84 = inst_22682;
                        inst_22682 = null;
                        inst_22683 = Numbers.add((Object)v83, (Object)v84);
                        v85 = inst_22655;
                        inst_22655 = null;
                        inst_22644 = v85;
                        v86 = inst_22683;
                        inst_22683 = null;
                        inst_22645 = v86;
                        statearr_22726 = this.state_22692;
                        v87 = inst_22644;
                        inst_22644 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22726, 19L, v87);
                        v88 = inst_22645;
                        inst_22645 = null;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22726, 21L, (Object)v88);
                        v89 = statearr_22726;
                        statearr_22726 = null;
                        v90 = state_22692 = v89;
                        state_22692 = null;
                        statearr_22727 = v90;
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22727, 2L, null);
                        ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22727, 1L, pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__1);
                        v7 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
                        break;
                    }
                    default: {
                        throw (Throwable)new IllegalArgumentException((String)((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__75.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__22701));
                    }
                }
                result__6363__auto__22731 = v7;
            } while (Util.identical((Object)result__6363__auto__22731, (Object)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37));
            v91 = result__6363__auto__22731;
            result__6363__auto__22731 = null;
            var74_71 = v91;
        }
        catch (Throwable ex__6364__auto__) {
            statearr_22728 = this.state_22692;
            ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22728, 2L, (Object)ex__6364__auto__);
            v92 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__24.getRawRoot()).invoke(((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 4L));
            if (v92 == null || v92 == Boolean.FALSE) {
                ex__6364__auto__ = null;
                throw ex__6364__auto__;
            }
            statearr_22729 = this.state_22692;
            ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(statearr_22729, 1L, ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__25.getRawRoot()).invoke(((IFn.OLO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__0.getRawRoot()).invokePrim(this.state_22692, 4L)));
            var74_71 = pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__37;
        }
        finally {
            this.state_22692 = null;
            ((IFn.OLOO)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700.const__10.getRawRoot()).invokePrim(this.state_22692, 3L, Var.getThreadBindingFrame());
            this.old_frame__6361__auto__ = null;
            Var.resetThreadBindingFrame((Object)this.old_frame__6361__auto__);
        }
        return var74_71;
    }

    static {
        const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aget-object");
        const__1 = 3L;
        const__3 = 1L;
        const__5 = 6L;
        const__6 = 7L;
        const__7 = RT.var((String)"clojure.core.async", (String)"timeout");
        const__8 = RT.var((String)"clojure.core", (String)"deref");
        const__9 = RT.var((String)"datomic.garbage.pod", (String)"pod-gc-delay-msec");
        const__10 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
        const__11 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"take!");
        const__12 = 2L;
        const__14 = 10L;
        const__15 = 14L;
        const__16 = 15L;
        const__17 = RT.var((String)"datomic.monitor", (String)"add-stat");
        const__18 = RT.keyword(null, (String)"PodGarbageItems");
        const__20 = RT.var((String)"clojure.core", (String)"mapv");
        const__21 = RT.var((String)"clojure.core", (String)"comp");
        const__22 = RT.var((String)"datomic.future", (String)"get-channel");
        const__23 = RT.var((String)"datomic.cluster", (String)"BOUNDING_TIMEOUT_MSEC");
        const__24 = RT.var((String)"clojure.core", (String)"seq");
        const__25 = RT.var((String)"clojure.core", (String)"first");
        const__26 = RT.var((String)"clojure.core", (String)"next");
        const__27 = 0L;
        const__29 = 8L;
        const__32 = 11L;
        const__37 = RT.keyword(null, (String)"recur");
        const__39 = 17L;
        const__40 = 12L;
        const__42 = 9L;
        const__43 = 13L;
        const__44 = 16L;
        const__46 = 5L;
        const__48 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"return-chan");
        const__50 = RT.var((String)"clojure.core", (String)"vector");
        const__51 = RT.var((String)"clojure.core.async", (String)"ioc-alts!");
        const__53 = RT.keyword(null, (String)"PodGarbageDeletedCount");
        const__55 = 4L;
        const__60 = RT.var((String)"datomic.garbage.pod", (String)"warn");
        const__65 = RT.keyword(null, (String)"ok");
        const__68 = RT.var((String)"datomic.process", (String)"fail");
        const__69 = RT.var((String)"datomic.process", (String)"instance");
        const__75 = RT.var((String)"clojure.core", (String)"str");
    }
}

