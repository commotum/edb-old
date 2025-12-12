/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.memcached.RecoveringClient;

public final class memcached$fn__10024$__GT_RecoveringClient__10031
extends AFunction {
    public Object invoke(Object client_ref, Object create_client2, Object sem) {
        Object object = client_ref;
        client_ref = null;
        Object object2 = create_client2;
        create_client2 = null;
        Object object3 = sem;
        sem = null;
        return new RecoveringClient(object, object2, object3);
    }
}

