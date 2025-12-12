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

public final class cache$fn__9379
extends AFunction {
    public static final Object const__0 = RT.classForName((String)"datomic.cache.ICachedLookup");

    public static Object invokeStatic() {
        return ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.cache.ICachedLookup"));
    }

    public Object invoke() {
        return cache$fn__9379.invokeStatic();
    }
}

