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

public final class extension_resolver$allow_list__GT_pred$fn__14314
extends AFunction {
    Object wp;
    Object ep;
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"qualified-symbol?");

    public extension_resolver$allow_list__GT_pred$fn__14314(Object object, Object object2) {
        this.wp = object;
        this.ep = object2;
    }

    public Object invoke(Object sym) {
        Object object;
        Object and__5236__auto__14317;
        Object object2 = and__5236__auto__14317 = ((IFn)const__0.getRawRoot()).invoke(sym);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object or__5238__auto__14316;
            Object object3 = or__5238__auto__14316 = ((IFn)this_.ep).invoke(sym);
            if (object3 != null && object3 != Boolean.FALSE) {
                object = or__5238__auto__14316;
                or__5238__auto__14316 = null;
            } else {
                Object object4 = sym;
                sym = null;
                extension_resolver$allow_list__GT_pred$fn__14314 this_ = null;
                object = ((IFn)this_.wp).invoke(object4);
            }
        } else {
            object = and__5236__auto__14317;
            Object var2_2 = null;
        }
        return object;
    }
}

