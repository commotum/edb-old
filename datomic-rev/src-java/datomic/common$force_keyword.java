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

public final class common$force_keyword
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"keyword?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object x) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(x);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = x;
            x = null;
        } else {
            Object object3 = x;
            x = null;
            object = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object3));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$force_keyword.invokeStatic(object2);
    }
}

