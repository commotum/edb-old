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
package datomic.core2.atom;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.atom.logged$append_value$fn__19744$G__19716__19745;
import datomic.core2.atom.logged$append_value$fn__19744$G__19717__19747;
import datomic.core2.atom.logged$append_value$fn__19744$G__19718__19749;
import datomic.core2.atom.logged$append_value$fn__19744$G__19719__19751;
import datomic.core2.atom.logged$append_value$fn__19744$state_machine__9975__auto____19753;

public final class logged$append_value$fn__19744
extends AFunction {
    Object c__10230__auto__;
    Object value;
    Object log;
    Object serialize;
    Object captured_bindings__10231__auto__;
    Object header;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public logged$append_value$fn__19744(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.c__10230__auto__ = object;
        this.value = object2;
        this.log = object3;
        this.serialize = object4;
        this.captured_bindings__10231__auto__ = object5;
        this.header = object6;
    }

    public Object invoke() {
        Object state__10233__auto__19774;
        logged$append_value$fn__19744$state_machine__9975__auto____19753 f__10232__auto__19773;
        this_.log = null;
        logged$append_value$fn__19744$G__19716__19745 G__19716 = new logged$append_value$fn__19744$G__19716__19745(this_.log);
        this_.serialize = null;
        logged$append_value$fn__19744$G__19717__19747 G__19717 = new logged$append_value$fn__19744$G__19717__19747(this_.serialize);
        this_.header = null;
        logged$append_value$fn__19744$G__19718__19749 G__19718 = new logged$append_value$fn__19744$G__19718__19749(this_.header);
        this_.value = null;
        logged$append_value$fn__19744$G__19719__19751 G__19719 = new logged$append_value$fn__19744$G__19719__19751(this_.value);
        logged$append_value$fn__19744$G__19717__19747 logged$append_value$fn__19744$G__19717__19747 = G__19717;
        G__19717 = null;
        logged$append_value$fn__19744$G__19719__19751 logged$append_value$fn__19744$G__19719__19751 = G__19719;
        G__19719 = null;
        logged$append_value$fn__19744$G__19718__19749 logged$append_value$fn__19744$G__19718__19749 = G__19718;
        G__19718 = null;
        logged$append_value$fn__19744$G__19716__19745 logged$append_value$fn__19744$G__19716__19745 = G__19716;
        G__19716 = null;
        logged$append_value$fn__19744$state_machine__9975__auto____19753 logged$append_value$fn__19744$state_machine__9975__auto____19753 = f__10232__auto__19773 = new logged$append_value$fn__19744$state_machine__9975__auto____19753((Object)logged$append_value$fn__19744$G__19717__19747, (Object)logged$append_value$fn__19744$G__19719__19751, (Object)logged$append_value$fn__19744$G__19718__19749, (Object)logged$append_value$fn__19744$G__19716__19745);
        f__10232__auto__19773 = null;
        Object statearr_19771 = ((IFn)logged$append_value$fn__19744$state_machine__9975__auto____19753).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_19771, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_19771, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_19771;
        statearr_19771 = null;
        Object object2 = state__10233__auto__19774 = object;
        state__10233__auto__19774 = null;
        logged$append_value$fn__19744 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

