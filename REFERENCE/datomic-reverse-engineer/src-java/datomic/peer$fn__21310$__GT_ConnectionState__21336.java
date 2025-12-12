/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.peer.ConnectionState;

public final class peer$fn__21310$__GT_ConnectionState__21336
extends AFunction {
    public Object invoke(Object connector2, Object notifier, Object updater, Object cleanup2) {
        Object object = connector2;
        connector2 = null;
        Object object2 = notifier;
        notifier = null;
        Object object3 = updater;
        updater = null;
        Object object4 = cleanup2;
        cleanup2 = null;
        return new ConnectionState(object, object2, object3, object4);
    }
}

