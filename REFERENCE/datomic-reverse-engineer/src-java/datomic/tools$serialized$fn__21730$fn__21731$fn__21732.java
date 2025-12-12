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

public final class tools$serialized$fn__21730$fn__21731$fn__21732
extends AFunction {
    Object args;
    Object f;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");

    public tools$serialized$fn__21730$fn__21731$fn__21732(Object object, Object object2) {
        this.args = object;
        this.f = object2;
    }

    public Object invoke() {
        Object object;
        try {
            this.f = null;
            this.args = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.f, this.args);
        }
        catch (Throwable t2) {
            Object t2 = null;
            t2.printStackTrace();
            object = null;
        }
        return object;
    }
}

