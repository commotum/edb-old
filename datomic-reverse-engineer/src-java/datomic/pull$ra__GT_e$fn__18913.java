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

public final class pull$ra__GT_e$fn__18913
extends AFunction {
    Object xf;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"conj!");

    public pull$ra__GT_e$fn__18913(Object object) {
        this.xf = object;
    }

    public Object invoke(Object coll, Object item) {
        Object object;
        Object temp__5455__auto__18915;
        Object object2 = item;
        item = null;
        Object object3 = temp__5455__auto__18915 = ((IFn)this_.xf).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5455__auto__18915;
            temp__5455__auto__18915 = null;
            Object xitem = object4;
            Object object5 = coll;
            coll = null;
            Object object6 = xitem;
            xitem = null;
            pull$ra__GT_e$fn__18913 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object5, object6);
        } else {
            object = coll;
            Object var1_1 = null;
        }
        return object;
    }
}

