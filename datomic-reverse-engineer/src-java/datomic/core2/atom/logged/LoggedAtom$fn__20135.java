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
import datomic.core2.atom.logged.LoggedAtom$fn__20135$G__20090__20136;
import datomic.core2.atom.logged.LoggedAtom$fn__20135$G__20091__20138;
import datomic.core2.atom.logged.LoggedAtom$fn__20135$G__20092__20140;
import datomic.core2.atom.logged.LoggedAtom$fn__20135$G__20093__20142;
import datomic.core2.atom.logged.LoggedAtom$fn__20135$G__20094__20144;
import datomic.core2.atom.logged.LoggedAtom$fn__20135$G__20095__20146;
import datomic.core2.atom.logged.LoggedAtom$fn__20135$G__20096__20148;
import datomic.core2.atom.logged.LoggedAtom$fn__20135$G__20097__20150;
import datomic.core2.atom.logged.LoggedAtom$fn__20135$G__20098__20152;
import datomic.core2.atom.logged.LoggedAtom$fn__20135$state_machine__9975__auto____20154;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class LoggedAtom$fn__20135
extends AFunction {
    Object serialize;
    Object state_ref;
    Object this;
    Object ch;
    Object validator_ref;
    Object deserialize;
    Object close_ch;
    Object captured_bindings__10231__auto__;
    Object c__10230__auto__;
    Object log;
    Object watches_ref;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public LoggedAtom$fn__20135(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11) {
        this.serialize = object;
        this.state_ref = object2;
        this.this = object3;
        this.ch = object4;
        this.validator_ref = object5;
        this.deserialize = object6;
        this.close_ch = object7;
        this.captured_bindings__10231__auto__ = object8;
        this.c__10230__auto__ = object9;
        this.log = object10;
        this.watches_ref = object11;
    }

    public Object invoke() {
        Object state__10233__auto__20170;
        LoggedAtom$fn__20135$state_machine__9975__auto____20154 f__10232__auto__20169;
        LoggedAtom$fn__20135$G__20090__20136 G__20090 = new LoggedAtom$fn__20135$G__20090__20136(this_.log);
        LoggedAtom$fn__20135$G__20091__20138 G__20091 = new LoggedAtom$fn__20135$G__20091__20138(this_.serialize);
        LoggedAtom$fn__20135$G__20092__20140 G__20092 = new LoggedAtom$fn__20135$G__20092__20140(this_.this);
        LoggedAtom$fn__20135$G__20093__20142 G__20093 = new LoggedAtom$fn__20135$G__20093__20142(this_.close_ch);
        LoggedAtom$fn__20135$G__20094__20144 G__20094 = new LoggedAtom$fn__20135$G__20094__20144(this_.state_ref);
        LoggedAtom$fn__20135$G__20095__20146 G__20095 = new LoggedAtom$fn__20135$G__20095__20146(this_.validator_ref);
        this_.ch = null;
        LoggedAtom$fn__20135$G__20096__20148 G__20096 = new LoggedAtom$fn__20135$G__20096__20148(this_.ch);
        LoggedAtom$fn__20135$G__20097__20150 G__20097 = new LoggedAtom$fn__20135$G__20097__20150(this_.deserialize);
        LoggedAtom$fn__20135$G__20098__20152 G__20098 = new LoggedAtom$fn__20135$G__20098__20152(this_.watches_ref);
        LoggedAtom$fn__20135$G__20092__20140 loggedAtom$fn__20135$G__20092__20140 = G__20092;
        G__20092 = null;
        LoggedAtom$fn__20135$G__20097__20150 loggedAtom$fn__20135$G__20097__20150 = G__20097;
        G__20097 = null;
        LoggedAtom$fn__20135$G__20091__20138 loggedAtom$fn__20135$G__20091__20138 = G__20091;
        G__20091 = null;
        LoggedAtom$fn__20135$G__20096__20148 loggedAtom$fn__20135$G__20096__20148 = G__20096;
        G__20096 = null;
        LoggedAtom$fn__20135$G__20098__20152 loggedAtom$fn__20135$G__20098__20152 = G__20098;
        G__20098 = null;
        LoggedAtom$fn__20135$G__20093__20142 loggedAtom$fn__20135$G__20093__20142 = G__20093;
        G__20093 = null;
        LoggedAtom$fn__20135$G__20094__20144 loggedAtom$fn__20135$G__20094__20144 = G__20094;
        G__20094 = null;
        LoggedAtom$fn__20135$G__20090__20136 loggedAtom$fn__20135$G__20090__20136 = G__20090;
        G__20090 = null;
        LoggedAtom$fn__20135$G__20095__20146 loggedAtom$fn__20135$G__20095__20146 = G__20095;
        G__20095 = null;
        LoggedAtom$fn__20135$state_machine__9975__auto____20154 loggedAtom$fn__20135$state_machine__9975__auto____20154 = f__10232__auto__20169 = new LoggedAtom$fn__20135$state_machine__9975__auto____20154((Object)loggedAtom$fn__20135$G__20092__20140, (Object)loggedAtom$fn__20135$G__20097__20150, (Object)loggedAtom$fn__20135$G__20091__20138, (Object)loggedAtom$fn__20135$G__20096__20148, (Object)loggedAtom$fn__20135$G__20098__20152, (Object)loggedAtom$fn__20135$G__20093__20142, (Object)loggedAtom$fn__20135$G__20094__20144, (Object)loggedAtom$fn__20135$G__20090__20136, (Object)loggedAtom$fn__20135$G__20095__20146);
        f__10232__auto__20169 = null;
        Object statearr_20167 = ((IFn)loggedAtom$fn__20135$state_machine__9975__auto____20154).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20167, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_20167, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_20167;
        statearr_20167 = null;
        Object object2 = state__10233__auto__20170 = object;
        state__10233__auto__20170 = null;
        LoggedAtom$fn__20135 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

