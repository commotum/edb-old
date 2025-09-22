/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.atom.logged;

import clojure.lang.AFunction;

public final class LoggedAtom$fn__20023$G__19943__20034
extends AFunction {
    Object validator_ref;

    public LoggedAtom$fn__20023$G__19943__20034(Object object) {
        this.validator_ref = object;
    }

    public Object invoke() {
        return this.validator_ref;
    }
}

