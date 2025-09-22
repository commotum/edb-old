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

public final class query$sort_for_pull$fn__19512
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");

    public Object invoke(Object idx, Object item) {
        Object object;
        Object object2 = item;
        item = null;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = idx;
            idx = null;
        } else {
            object = null;
        }
        return object;
    }
}

