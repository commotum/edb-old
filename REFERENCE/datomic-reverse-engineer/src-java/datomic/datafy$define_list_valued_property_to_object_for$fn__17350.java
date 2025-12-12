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

public final class datafy$define_list_valued_property_to_object_for$fn__17350
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"datomic.datafy", (String)"define-list-valued-property-to-object");

    public Object invoke(Object p1__17349_SHARP_) {
        Object object = p1__17349_SHARP_;
        p1__17349_SHARP_ = null;
        datafy$define_list_valued_property_to_object_for$fn__17350 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), object);
    }
}

