/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.Attribute;

public final class index$needs_new_avet$fn__15584
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");

    public Object invoke(Object a) {
        Object object;
        Object and__5236__auto__15586;
        Object object2 = and__5236__auto__15586 = ((Attribute)a).needsAVET;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = a;
            a = null;
            index$needs_new_avet$fn__15584 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(((Attribute)object3).hasAVET());
        } else {
            object = and__5236__auto__15586;
            Object var2_2 = null;
        }
        return object;
    }
}

