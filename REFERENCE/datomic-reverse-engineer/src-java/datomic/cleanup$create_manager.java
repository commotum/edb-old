/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cleanup.Manager;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;

public final class cleanup$create_manager
extends AFunction {
    public static Object invokeStatic() {
        return new Manager(new ConcurrentHashMap(), new ReferenceQueue());
    }

    public Object invoke() {
        return cleanup$create_manager.invokeStatic();
    }
}

