/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic.core2.atom.logged;

import clojure.lang.AFunction;
import clojure.lang.IFn;

public final class LoggedAtom$fn__19780
extends AFunction {
    Object v;
    Object validator;

    public LoggedAtom$fn__19780(Object object, Object object2) {
        this.v = object;
        this.validator = object2;
    }

    public Object invoke() {
        Boolean bl;
        try {
            ((IFn)this.validator).invoke(this.v);
            bl = Boolean.TRUE;
        }
        catch (Throwable t) {
            bl = Boolean.FALSE;
        }
        return bl;
    }
}

