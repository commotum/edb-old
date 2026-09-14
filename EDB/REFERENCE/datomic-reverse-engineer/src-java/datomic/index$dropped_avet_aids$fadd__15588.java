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
import datomic.index$dropped_avet_aids$fadd__15588$fn__15589;

public final class index$dropped_avet_aids$fadd__15588
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public index$dropped_avet_aids$fadd__15588(Object object) {
        this.db = object;
    }

    public Object invoke(Object aids, Object ds) {
        Object object = aids;
        aids = null;
        Object object2 = ds;
        ds = null;
        index$dropped_avet_aids$fadd__15588 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new index$dropped_avet_aids$fadd__15588$fn__15589(this_.db), object, object2);
    }
}

