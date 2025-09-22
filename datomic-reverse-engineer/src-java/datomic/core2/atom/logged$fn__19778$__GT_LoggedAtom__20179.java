/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.atom;

import clojure.lang.AFunction;
import datomic.core2.atom.logged.LoggedAtom;

public final class logged$fn__19778$__GT_LoggedAtom__20179
extends AFunction {
    public Object invoke(Object log2, Object close_ch, Object state_ref, Object serialize, Object deserialize2, Object validator_ref, Object watches_ref) {
        Object object = log2;
        log2 = null;
        Object object2 = close_ch;
        close_ch = null;
        Object object3 = state_ref;
        state_ref = null;
        Object object4 = serialize;
        serialize = null;
        Object object5 = deserialize2;
        deserialize2 = null;
        Object object6 = validator_ref;
        validator_ref = null;
        Object object7 = watches_ref;
        watches_ref = null;
        return new LoggedAtom(object, object2, object3, object4, object5, object6, object7);
    }
}

