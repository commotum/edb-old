/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.atom.logged;

import clojure.lang.AFunction;

public final class LoggedAtom$fn__20023$G__19942__20032
extends AFunction {
    Object state_ref;

    public LoggedAtom$fn__20023$G__19942__20032(Object object) {
        this.state_ref = object;
    }

    public Object invoke() {
        return this.state_ref;
    }
}

