/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.atom.logged;

import clojure.lang.AFunction;

public final class LoggedAtom$fn__20023$G__19946__20040
extends AFunction {
    Object watches_ref;

    public LoggedAtom$fn__20023$G__19946__20040(Object object) {
        this.watches_ref = object;
    }

    public Object invoke() {
        return this.watches_ref;
    }
}

