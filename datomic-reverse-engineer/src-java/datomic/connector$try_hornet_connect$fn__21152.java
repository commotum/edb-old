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
import datomic.artemis_client.SessionFactoryBundle;

public final class connector$try_hornet_connect$fn__21152
extends AFunction {
    Object bundle;

    public connector$try_hornet_connect$fn__21152(Object object) {
        this.bundle = object;
    }

    public Object invoke() {
        connector$try_hornet_connect$fn__21152 this_ = null;
        return ((IFn)((SessionFactoryBundle)this_.bundle).cleanup).invoke();
    }
}

