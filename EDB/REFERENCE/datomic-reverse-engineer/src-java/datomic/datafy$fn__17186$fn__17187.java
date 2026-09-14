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

public final class datafy$fn__17186$fn__17187
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"type-descriptor-category");

    public Object invoke(Object container, Object prop, Object type) {
        Object object = type;
        type = null;
        datafy$fn__17186$fn__17187 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object);
    }
}

