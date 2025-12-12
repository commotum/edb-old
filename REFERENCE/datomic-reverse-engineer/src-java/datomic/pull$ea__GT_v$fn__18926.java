/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class pull$ea__GT_v$fn__18926
extends AFunction {
    Object xf;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"conj!");

    public pull$ea__GT_v$fn__18926(Object object) {
        this.xf = object;
    }

    public Object invoke(Object coll, Object item) {
        Object object;
        Object object2 = item;
        item = null;
        Object xitem = ((IFn)this_.xf).invoke(object2);
        if (Util.identical((Object)xitem, null)) {
            object = coll;
            coll = null;
        } else {
            Object object3 = coll;
            coll = null;
            Object object4 = xitem;
            xitem = null;
            pull$ea__GT_v$fn__18926 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, object4);
        }
        return object;
    }
}

