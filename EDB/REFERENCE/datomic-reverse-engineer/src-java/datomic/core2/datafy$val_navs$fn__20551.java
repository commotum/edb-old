/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;

public final class datafy$val_navs$fn__20551
extends AFunction {
    Object k__GT_f;

    public datafy$val_navs$fn__20551(Object object) {
        this.k__GT_f = object;
    }

    public Object invoke(Object _, Object k, Object v) {
        Object object;
        Object temp__5802__auto__20553;
        Object object2 = k;
        k = null;
        Object object3 = temp__5802__auto__20553 = ((IFn)this_.k__GT_f).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object f;
            Object object4 = temp__5802__auto__20553;
            temp__5802__auto__20553 = null;
            Object object5 = f = object4;
            f = null;
            Object object6 = v;
            v = null;
            datafy$val_navs$fn__20551 this_ = null;
            object = ((IFn)object5).invoke(object6);
        } else {
            object = v;
            Object var3_3 = null;
        }
        return object;
    }
}

