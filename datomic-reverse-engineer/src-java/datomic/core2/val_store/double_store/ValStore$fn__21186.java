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
package datomic.core2.val_store.double_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.val_store.double_store.ValStore$fn__21186$G__21087__21187;
import datomic.core2.val_store.double_store.ValStore$fn__21186$G__21088__21189;
import datomic.core2.val_store.double_store.ValStore$fn__21186$G__21089__21191;
import datomic.core2.val_store.double_store.ValStore$fn__21186$G__21090__21193;
import datomic.core2.val_store.double_store.ValStore$fn__21186$G__21091__21195;
import datomic.core2.val_store.double_store.ValStore$fn__21186$G__21092__21197;
import datomic.core2.val_store.double_store.ValStore$fn__21186$G__21093__21199;
import datomic.core2.val_store.double_store.ValStore$fn__21186$state_machine__9975__auto____21201;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class ValStore$fn__21186
extends AFunction {
    Object captured_bindings__10231__auto__;
    Object opts;
    Object k;
    Object this;
    Object far_store;
    Object c__10230__auto__;
    Object get_fallback_msec;
    Object near_store;
    Object repair_metric;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public ValStore$fn__21186(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.captured_bindings__10231__auto__ = object;
        this.opts = object2;
        this.k = object3;
        this.this = object4;
        this.far_store = object5;
        this.c__10230__auto__ = object6;
        this.get_fallback_msec = object7;
        this.near_store = object8;
        this.repair_metric = object9;
    }

    public Object invoke() {
        Object state__10233__auto__21244;
        ValStore$fn__21186$state_machine__9975__auto____21201 f__10232__auto__21243;
        ValStore$fn__21186$G__21087__21187 G__21087 = new ValStore$fn__21186$G__21087__21187(this_.this);
        ValStore$fn__21186$G__21088__21189 G__21088 = new ValStore$fn__21186$G__21088__21189(this_.get_fallback_msec);
        ValStore$fn__21186$G__21089__21191 G__21089 = new ValStore$fn__21186$G__21089__21191(this_.near_store);
        ValStore$fn__21186$G__21090__21193 G__21090 = new ValStore$fn__21186$G__21090__21193(this_.repair_metric);
        this_.k = null;
        ValStore$fn__21186$G__21091__21195 G__21091 = new ValStore$fn__21186$G__21091__21195(this_.k);
        ValStore$fn__21186$G__21092__21197 G__21092 = new ValStore$fn__21186$G__21092__21197(this_.far_store);
        this_.opts = null;
        ValStore$fn__21186$G__21093__21199 G__21093 = new ValStore$fn__21186$G__21093__21199(this_.opts);
        ValStore$fn__21186$G__21087__21187 valStore$fn__21186$G__21087__21187 = G__21087;
        G__21087 = null;
        ValStore$fn__21186$G__21090__21193 valStore$fn__21186$G__21090__21193 = G__21090;
        G__21090 = null;
        ValStore$fn__21186$G__21091__21195 valStore$fn__21186$G__21091__21195 = G__21091;
        G__21091 = null;
        ValStore$fn__21186$G__21088__21189 valStore$fn__21186$G__21088__21189 = G__21088;
        G__21088 = null;
        ValStore$fn__21186$G__21089__21191 valStore$fn__21186$G__21089__21191 = G__21089;
        G__21089 = null;
        ValStore$fn__21186$G__21092__21197 valStore$fn__21186$G__21092__21197 = G__21092;
        G__21092 = null;
        ValStore$fn__21186$G__21093__21199 valStore$fn__21186$G__21093__21199 = G__21093;
        G__21093 = null;
        ValStore$fn__21186$state_machine__9975__auto____21201 valStore$fn__21186$state_machine__9975__auto____21201 = f__10232__auto__21243 = new ValStore$fn__21186$state_machine__9975__auto____21201((Object)valStore$fn__21186$G__21087__21187, (Object)valStore$fn__21186$G__21090__21193, (Object)valStore$fn__21186$G__21091__21195, (Object)valStore$fn__21186$G__21088__21189, (Object)valStore$fn__21186$G__21089__21191, (Object)valStore$fn__21186$G__21092__21197, (Object)valStore$fn__21186$G__21093__21199);
        f__10232__auto__21243 = null;
        Object statearr_21241 = ((IFn)valStore$fn__21186$state_machine__9975__auto____21201).invoke();
        this_.c__10230__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_21241, 5L, this_.c__10230__auto__);
        this_.captured_bindings__10231__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_21241, 3L, this_.captured_bindings__10231__auto__);
        Object object = statearr_21241;
        statearr_21241 = null;
        Object object2 = state__10233__auto__21244 = object;
        state__10233__auto__21244 = null;
        ValStore$fn__21186 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

