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
import datomic.slf4j$fn__9005$fn__9006;

public final class slf4j$fn__9005
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapv");

    public static Object invokeStatic(Object data2, Object redact_QMARK_) {
        Object object = redact_QMARK_;
        redact_QMARK_ = null;
        Object object2 = data2;
        data2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new slf4j$fn__9005$fn__9006(object), object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return slf4j$fn__9005.invokeStatic(object3, object4);
    }
}

