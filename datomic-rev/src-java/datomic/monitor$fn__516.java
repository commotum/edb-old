/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Namespace
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Namespace;
import clojure.lang.RT;

public final class monitor$fn__516
extends AFunction {
    public static final Object const__0 = RT.classForName((String)"datomic.monitor.StatsUpdate");

    public static Object invokeStatic() {
        return ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.monitor.StatsUpdate"));
    }

    public Object invoke() {
        return monitor$fn__516.invokeStatic();
    }
}

