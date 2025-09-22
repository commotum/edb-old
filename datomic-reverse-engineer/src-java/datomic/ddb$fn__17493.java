/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.services.dynamodbv2.model.ReturnValue
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

public final class ddb$fn__17493
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"name");

    public static Object invokeStatic(Object n, Object _) {
        Object object = n;
        n = null;
        return ReturnValue.valueOf((String)((String)((IFn)const__0.getRawRoot()).invoke(object)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return ddb$fn__17493.invokeStatic(object3, object4);
    }
}

