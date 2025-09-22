/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import datomic.tools$serialized$fn__21730$fn__21731$fn__21732;

public final class tools$serialized$fn__21730$fn__21731
extends AFunction {
    Object args;
    Object f;

    public tools$serialized$fn__21730$fn__21731(Object object, Object object2) {
        this.args = object;
        this.f = object2;
    }

    public Object invoke(Object _) {
        ((IFn)new tools$serialized$fn__21730$fn__21731$fn__21732(this.args, this.f)).invoke();
        return null;
    }
}

