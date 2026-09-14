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

public final class log$extend_tree$create__16558
extends AFunction {
    Object cs;
    public static final Var const__0 = RT.var((String)"datomic.log", (String)"zip-and-create");

    public log$extend_tree$create__16558(Object object) {
        this.cs = object;
    }

    public Object invoke(Object uuid, Object val) {
        Object object = uuid;
        uuid = null;
        Object object2 = val;
        val = null;
        log$extend_tree$create__16558 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.cs, object, object2);
    }
}

