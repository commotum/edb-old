/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import datomic.artemis_client$create_session_factory$fn__20822$fn__20823;

public final class artemis_client$create_session_factory$fn__20822
extends AFunction {
    Object session_factory;
    Object cleanup;

    public artemis_client$create_session_factory$fn__20822(Object object, Object object2) {
        this.session_factory = object;
        this.cleanup = object2;
    }

    public Object invoke() {
        ((IFn)new artemis_client$create_session_factory$fn__20822$fn__20823(this_.session_factory)).invoke();
        artemis_client$create_session_factory$fn__20822 this_ = null;
        return ((IFn)this_.cleanup).invoke();
    }
}

