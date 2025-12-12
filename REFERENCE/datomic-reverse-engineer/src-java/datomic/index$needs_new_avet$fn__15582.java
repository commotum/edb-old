/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.Attribute;

public final class index$needs_new_avet$fn__15582
extends AFunction {
    public Object invoke(Object a) {
        Object object = a;
        a = null;
        return ((Attribute)object).id();
    }
}

