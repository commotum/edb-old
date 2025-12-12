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

public final class aws_monitor$fn__23585
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"name");

    public static Object invokeStatic(Object kw) {
        Object object;
        Object temp__5455__auto__23587;
        Object object2 = temp__5455__auto__23587 = ((IFn)const__0.getRawRoot()).invoke(kw);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object ns;
            Object object3 = temp__5455__auto__23587;
            temp__5455__auto__23587 = null;
            Object object4 = ns = object3;
            ns = null;
            Object object5 = kw;
            kw = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object4, (Object)"/", ((IFn)const__2.getRawRoot()).invoke(object5));
        } else {
            Object object6 = kw;
            kw = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object6);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aws_monitor$fn__23585.invokeStatic(object2);
    }
}

