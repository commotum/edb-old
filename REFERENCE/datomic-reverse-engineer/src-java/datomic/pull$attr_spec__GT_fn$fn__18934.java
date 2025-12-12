/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class pull$attr_spec__GT_fn$fn__18934
extends AFunction {
    Object arg;

    public pull$attr_spec__GT_fn$fn__18934(Object object) {
        this.arg = object;
    }

    public Object invoke(Object v) {
        Object object;
        Object or__5238__auto__18936;
        Object object2 = v;
        v = null;
        Object object3 = or__5238__auto__18936 = object2;
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__18936;
            or__5238__auto__18936 = null;
        } else {
            object = this.arg;
        }
        return object;
    }
}

