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
import datomic.core2.atom.logged.LoggedAtom$fn__19869$G__19782__19870;
import datomic.core2.atom.logged.LoggedAtom$fn__19869$G__19783__19872;
import datomic.core2.atom.logged.LoggedAtom$fn__19869$G__19784__19874;
import datomic.core2.atom.logged.LoggedAtom$fn__19869$G__19785__19876;
import datomic.core2.atom.logged.LoggedAtom$fn__19869$G__19786__19878;
import datomic.core2.atom.logged.LoggedAtom$fn__19869$G__19787__19880;
import datomic.core2.atom.logged.LoggedAtom$fn__19869$G__19788__19882;
import datomic.core2.atom.logged.LoggedAtom$fn__19869$G__19789__19884;
import datomic.core2.atom.logged.LoggedAtom$fn__19869$state_machine__9975__auto____19886;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class LoggedAtom$fn__19869
extends AFunction {
    Object this;
    Object serialize;
    Object state_ref;
    Object validator_ref;
    Object deserialize;
    Object close_ch;
    Object c__10230__auto__;
    Object log;
    Object watches_ref;
    Object captured_bindings__10231__auto__;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public LoggedAtom$fn__19869(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.this = object;
        this.serialize = object2;
        this.state_ref = object3;
        this.validator_ref = object4;
        this.deserialize = object5;
        this.close_ch = object6;
        this.c__10230__auto__ = object7;
        this.log = object8;
        this.watches_ref = object9;
        this.captured_bindings__10231__auto__ = object10;
    }

    public Object invoke() {
        Object state__10233__auto__19937;
        LoggedAtom$fn__19869$state_machine__9975__auto____19886 f__10232__auto__19936;
        LoggedAtom$fn__19869$G__19782__19870 G__19782 = new LoggedAtom$fn__19869$G__19782__19870(this_.log);
        LoggedAtom$fn__19869$G__19783__19872 G__19783 = new LoggedAtom$fn__19869$G__19783__19872(this_.serialize);
        LoggedAtom$fn__19869$G__19784__19874 G__19784 = new LoggedAtom$fn__19869$G__19784__19874(this_.this);
        LoggedAtom$fn__19869$G__19785__19876 G__19785 = new LoggedAtom$fn__19869$G__19785__19876(this_.close_ch);
        LoggedAtom$fn__19869$G__19786__19878 G__19786 = new LoggedAtom$fn__19869$G__19786__19878(this_.state_ref);
        LoggedAtom$fn__19869$G__19787__19880 G__19787 = new LoggedAtom$fn__19869$G__19787__19880(this_.validator_ref);
        LoggedAtom$fn__19869$G__19788__19882 G__19788 = new LoggedAtom$fn__19869$G__19788__19882(this_.deserialize);
        LoggedAtom$fn__19869$G__19789__19884 G__19789 = new LoggedAtom$fn__19869$G__19789__19884(this_.watches_ref);
        LoggedAtom$fn__19869$G__19785__19876 loggedAtom$fn__19869$G__19785__19876 = G__19785;
        G__19785 = null;
        LoggedAtom$fn__19869$G__19786__19878 loggedAtom$fn__19869$G__19786__19878 = G__19786;
        G__19786 = null;
        LoggedAtom$fn__19869$G__19788__19882 loggedAtom$fn__19869$G__19788__19882 = G__19788;
        G__19788 = null;
        LoggedAtom$fn__19869$G__19789__19884 loggedAtom$fn__19869$G__19789__19884 = G__19789;
        G__19789 = null;
        LoggedAtom$fn__19869$G__19783__19872 loggedAtom$fn__19869$G__19783__19872 = G__19783;
        G__19783 = null;
        LoggedAtom$fn__19869$G__19787__19880 loggedAtom$fn__19869$G__19787__19880 = G__19787;
        G__19787 = null;
        LoggedAtom$fn__19869$G__19782__19870 loggedAtom$fn__19869$G__19782__19870 = G__19782;
        G__19782 = null;
        LoggedAtom$fn__19869$G__19784__19874 loggedAtom$fn__19869$G__19784__19874 = G__19784;
        G__19784 = null;
        LoggedAtom$fn__19869$state_machine__9975__auto____19886 loggedAtom$fn__19869$state_machine__9975__auto____19886 = f__10232__auto__19936 = new LoggedAtom$fn__19869$state_machine__9975__auto____19886((Object)loggedAtom$fn__19869$G__19785__19876, (Object)loggedAtom$fn__19869$G__19786__19878, (Object)loggedAtom$fn__19869$G__19788__19882, (Object)loggedAtom$fn__19869$G__19789__19884, (Object)loggedAtom$fn__19869$G__19783__19872, (Object)loggedAtom$fn__19869$G__19787__19880, (Object)loggedAtom$fn__19869$G__19782__19870, (Object)loggedAtom$fn__19869$G__19784__19874);
        f__10232__auto__19936 = null;
        Object statearr_19934 = ((IFn)loggedAtom$fn__19869$state_machine__9975__auto____19886).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_19934, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_19934, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_19934;
        statearr_19934 = null;
        Object object2 = state__10233__auto__19937 = object;
        state__10233__auto__19937 = null;
        LoggedAtom$fn__19869 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

