/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLOO
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.atom.logged;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.atom.logged.LoggedAtom$fn__20023$G__19938__20024;
import datomic.core2.atom.logged.LoggedAtom$fn__20023$G__19939__20026;
import datomic.core2.atom.logged.LoggedAtom$fn__20023$G__19940__20028;
import datomic.core2.atom.logged.LoggedAtom$fn__20023$G__19941__20030;
import datomic.core2.atom.logged.LoggedAtom$fn__20023$G__19942__20032;
import datomic.core2.atom.logged.LoggedAtom$fn__20023$G__19943__20034;
import datomic.core2.atom.logged.LoggedAtom$fn__20023$G__19944__20036;
import datomic.core2.atom.logged.LoggedAtom$fn__20023$G__19945__20038;
import datomic.core2.atom.logged.LoggedAtom$fn__20023$G__19946__20040;
import datomic.core2.atom.logged.LoggedAtom$fn__20023$G__19947__20042;
import datomic.core2.atom.logged.LoggedAtom$fn__20023$state_machine__9975__auto____20044;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class LoggedAtom$fn__20023
extends AFunction {
    Object captured_bindings__10231__auto__;
    Object ch;
    Object serialize;
    Object state_ref;
    Object c__10230__auto__;
    Object validator_ref;
    Object this;
    Object f;
    Object deserialize;
    Object close_ch;
    Object log;
    Object watches_ref;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public LoggedAtom$fn__20023(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12) {
        this.captured_bindings__10231__auto__ = object;
        this.ch = object2;
        this.serialize = object3;
        this.state_ref = object4;
        this.c__10230__auto__ = object5;
        this.validator_ref = object6;
        this.this = object7;
        this.f = object8;
        this.deserialize = object9;
        this.close_ch = object10;
        this.log = object11;
        this.watches_ref = object12;
    }

    public Object invoke() {
        Object state__10233__auto__20089;
        LoggedAtom$fn__20023$state_machine__9975__auto____20044 f__10232__auto__20088;
        LoggedAtom$fn__20023$G__19938__20024 G__19938 = new LoggedAtom$fn__20023$G__19938__20024(this_.log);
        LoggedAtom$fn__20023$G__19939__20026 G__19939 = new LoggedAtom$fn__20023$G__19939__20026(this_.serialize);
        LoggedAtom$fn__20023$G__19940__20028 G__19940 = new LoggedAtom$fn__20023$G__19940__20028(this_.this);
        LoggedAtom$fn__20023$G__19941__20030 G__19941 = new LoggedAtom$fn__20023$G__19941__20030(this_.close_ch);
        LoggedAtom$fn__20023$G__19942__20032 G__19942 = new LoggedAtom$fn__20023$G__19942__20032(this_.state_ref);
        LoggedAtom$fn__20023$G__19943__20034 G__19943 = new LoggedAtom$fn__20023$G__19943__20034(this_.validator_ref);
        this_.ch = null;
        LoggedAtom$fn__20023$G__19944__20036 G__19944 = new LoggedAtom$fn__20023$G__19944__20036(this_.ch);
        LoggedAtom$fn__20023$G__19945__20038 G__19945 = new LoggedAtom$fn__20023$G__19945__20038(this_.deserialize);
        LoggedAtom$fn__20023$G__19946__20040 G__19946 = new LoggedAtom$fn__20023$G__19946__20040(this_.watches_ref);
        this_.f = null;
        LoggedAtom$fn__20023$G__19947__20042 G__19947 = new LoggedAtom$fn__20023$G__19947__20042(this_.f);
        LoggedAtom$fn__20023$G__19938__20024 loggedAtom$fn__20023$G__19938__20024 = G__19938;
        G__19938 = null;
        LoggedAtom$fn__20023$G__19940__20028 loggedAtom$fn__20023$G__19940__20028 = G__19940;
        G__19940 = null;
        LoggedAtom$fn__20023$G__19944__20036 loggedAtom$fn__20023$G__19944__20036 = G__19944;
        G__19944 = null;
        LoggedAtom$fn__20023$G__19943__20034 loggedAtom$fn__20023$G__19943__20034 = G__19943;
        G__19943 = null;
        LoggedAtom$fn__20023$G__19945__20038 loggedAtom$fn__20023$G__19945__20038 = G__19945;
        G__19945 = null;
        LoggedAtom$fn__20023$G__19939__20026 loggedAtom$fn__20023$G__19939__20026 = G__19939;
        G__19939 = null;
        LoggedAtom$fn__20023$G__19947__20042 loggedAtom$fn__20023$G__19947__20042 = G__19947;
        G__19947 = null;
        LoggedAtom$fn__20023$G__19941__20030 loggedAtom$fn__20023$G__19941__20030 = G__19941;
        G__19941 = null;
        LoggedAtom$fn__20023$G__19942__20032 loggedAtom$fn__20023$G__19942__20032 = G__19942;
        G__19942 = null;
        LoggedAtom$fn__20023$G__19946__20040 loggedAtom$fn__20023$G__19946__20040 = G__19946;
        G__19946 = null;
        LoggedAtom$fn__20023$state_machine__9975__auto____20044 loggedAtom$fn__20023$state_machine__9975__auto____20044 = f__10232__auto__20088 = new LoggedAtom$fn__20023$state_machine__9975__auto____20044((Object)loggedAtom$fn__20023$G__19938__20024, (Object)loggedAtom$fn__20023$G__19940__20028, (Object)loggedAtom$fn__20023$G__19944__20036, (Object)loggedAtom$fn__20023$G__19943__20034, (Object)loggedAtom$fn__20023$G__19945__20038, (Object)loggedAtom$fn__20023$G__19939__20026, (Object)loggedAtom$fn__20023$G__19947__20042, (Object)loggedAtom$fn__20023$G__19941__20030, (Object)loggedAtom$fn__20023$G__19942__20032, (Object)loggedAtom$fn__20023$G__19946__20040);
        f__10232__auto__20088 = null;
        Object statearr_20086 = ((IFn)loggedAtom$fn__20023$state_machine__9975__auto____20044).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20086, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20086, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_20086;
        statearr_20086 = null;
        Object object2 = state__10233__auto__20089 = object;
        state__10233__auto__20089 = null;
        LoggedAtom$fn__20023 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

