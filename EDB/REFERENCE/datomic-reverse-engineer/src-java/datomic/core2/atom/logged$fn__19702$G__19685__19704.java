/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.atom;

import clojure.lang.AFunction;
import datomic.core2.atom.logged.LoggedAtomImpl;

public final class logged$fn__19702$G__19685__19704
extends AFunction {
    public Object invoke(Object gf_____19703) {
        Object object = gf_____19703;
        gf_____19703 = null;
        return ((LoggedAtomImpl)object)._read_latest();
    }
}

