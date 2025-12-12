/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.artemis_client.SessionFactoryBundle;

public final class artemis_client$fn__20805$__GT_SessionFactoryBundle__20811
extends AFunction {
    public Object invoke(Object locator, Object factory, Object cleanup2) {
        Object object = locator;
        locator = null;
        Object object2 = factory;
        factory = null;
        Object object3 = cleanup2;
        cleanup2 = null;
        return new SessionFactoryBundle(object, object2, object3);
    }
}

