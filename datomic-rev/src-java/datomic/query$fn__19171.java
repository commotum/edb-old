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

public final class query$fn__19171
extends AFunction {
    public static final Object const__0 = RT.classForName((String)"datomic.query.EMapImpl");

    public static Object invokeStatic() {
        return ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.query.EMapImpl"));
    }

    public Object invoke() {
        return query$fn__19171.invokeStatic();
    }
}

