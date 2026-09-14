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
package datomic.garbage;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.garbage.pod$schedule_gc$fn__22693$G__22600__22694;
import datomic.garbage.pod$schedule_gc$fn__22693$G__22601__22696;
import datomic.garbage.pod$schedule_gc$fn__22693$state_machine__6360__auto____22698;

public final class pod$schedule_gc$fn__22693
extends AFunction {
    Object c__6597__auto__;
    Object garbage_ids_ref;
    Object captured_bindings__6598__auto__;
    Object cluster;
    public static final Var const__0 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"aset-object");
    public static final Var const__3 = RT.var((String)"clojure.core.async.impl.ioc-macros", (String)"run-state-machine-wrapped");

    public pod$schedule_gc$fn__22693(Object object, Object object2, Object object3, Object object4) {
        this.c__6597__auto__ = object;
        this.garbage_ids_ref = object2;
        this.captured_bindings__6598__auto__ = object3;
        this.cluster = object4;
    }

    public Object invoke() {
        Object state__6600__auto__22738;
        pod$schedule_gc$fn__22693$state_machine__6360__auto____22698 f__6599__auto__22737;
        pod$schedule_gc$fn__22693$G__22601__22696 G__22601;
        this_.cluster = null;
        pod$schedule_gc$fn__22693$G__22600__22694 G__22600 = new pod$schedule_gc$fn__22693$G__22600__22694(this_.cluster);
        this_.garbage_ids_ref = null;
        pod$schedule_gc$fn__22693$G__22601__22696 pod$schedule_gc$fn__22693$G__22601__22696 = G__22601 = new pod$schedule_gc$fn__22693$G__22601__22696(this_.garbage_ids_ref);
        G__22601 = null;
        pod$schedule_gc$fn__22693$G__22600__22694 pod$schedule_gc$fn__22693$G__22600__22694 = G__22600;
        G__22600 = null;
        pod$schedule_gc$fn__22693$state_machine__6360__auto____22698 pod$schedule_gc$fn__22693$state_machine__6360__auto____22698 = f__6599__auto__22737 = new pod$schedule_gc$fn__22693$state_machine__6360__auto____22698((Object)pod$schedule_gc$fn__22693$G__22601__22696, (Object)pod$schedule_gc$fn__22693$G__22600__22694);
        f__6599__auto__22737 = null;
        Object statearr_22735 = ((IFn)pod$schedule_gc$fn__22693$state_machine__6360__auto____22698).invoke();
        this_.c__6597__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_22735, 5L, this_.c__6597__auto__);
        this_.captured_bindings__6598__auto__ = null;
        ((IFn.OLOO)const__0.getRawRoot()).invokePrim(statearr_22735, 3L, this_.captured_bindings__6598__auto__);
        Object object = statearr_22735;
        statearr_22735 = null;
        Object object2 = state__6600__auto__22738 = object;
        state__6600__auto__22738 = null;
        pod$schedule_gc$fn__22693 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object2);
    }
}

