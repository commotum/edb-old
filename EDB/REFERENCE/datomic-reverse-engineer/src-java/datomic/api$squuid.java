/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Peer;

public final class api$squuid
extends AFunction {
    public static Object invokeStatic() {
        return Peer.squuid();
    }

    public Object invoke() {
        return api$squuid.invokeStatic();
    }
}

