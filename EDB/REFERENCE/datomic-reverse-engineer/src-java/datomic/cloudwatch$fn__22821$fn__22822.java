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

public final class cloudwatch$fn__22821$fn__22822
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"data-to-object");
    public static final Object const__1 = RT.classForName((String)"com.amazonaws.services.cloudwatch.model.Dimension");

    public Object invoke(Object item) {
        Object object = item;
        item = null;
        cloudwatch$fn__22821$fn__22822 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1);
    }
}

