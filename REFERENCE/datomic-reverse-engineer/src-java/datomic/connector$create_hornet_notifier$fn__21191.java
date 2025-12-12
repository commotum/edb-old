/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.connector$create_hornet_notifier$fn__21191$fn__21192;

public final class connector$create_hornet_notifier$fn__21191
extends AFunction {
    Object cleanup_ref;
    Object done_ref;
    Object push_handler_ref;
    Object hornet_consumer;
    Object failure_handler;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"future-call");

    public connector$create_hornet_notifier$fn__21191(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.cleanup_ref = object;
        this.done_ref = object2;
        this.push_handler_ref = object3;
        this.hornet_consumer = object4;
        this.failure_handler = object5;
    }

    public Object invoke() {
        this_.cleanup_ref = null;
        this_.done_ref = null;
        this_.push_handler_ref = null;
        this_.hornet_consumer = null;
        this_.failure_handler = null;
        connector$create_hornet_notifier$fn__21191 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new connector$create_hornet_notifier$fn__21191$fn__21192(this_.cleanup_ref, this_.done_ref, this_.push_handler_ref, this_.hornet_consumer, this_.failure_handler));
    }
}

