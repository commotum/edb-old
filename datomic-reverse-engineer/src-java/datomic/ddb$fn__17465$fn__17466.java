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

public final class ddb$fn__17465$fn__17466
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"data-to-object");
    public static final Object const__1 = RT.classForName((String)"com.amazonaws.services.dynamodbv2.model.AttributeDefinition");

    public Object invoke(Object item) {
        Object object = item;
        item = null;
        ddb$fn__17465$fn__17466 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1);
    }
}

