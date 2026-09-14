/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class garbage$install_mark_handler$fn__19819
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.domain", (String)"system-cache-olookup");

    public Object invoke(Object cluster2) {
        Object object = cluster2;
        cluster2 = null;
        garbage$install_mark_handler$fn__19819 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }
}

