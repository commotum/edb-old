/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.atom;

import clojure.lang.AFunction;
import datomic.core2.atom.logged.LoggedAtomImpl;

public final class logged$fn__19689$G__19683__19692
extends AFunction {
    public Object invoke(Object gf_____19690, Object gf__v__19691) {
        Object object = gf_____19690;
        gf_____19690 = null;
        Object object2 = gf__v__19691;
        gf__v__19691 = null;
        return ((LoggedAtomImpl)object)._validated_v(object2);
    }
}

