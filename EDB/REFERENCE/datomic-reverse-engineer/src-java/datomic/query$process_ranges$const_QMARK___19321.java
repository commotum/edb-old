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

public final class query$process_ranges$const_QMARK___19321
extends AFunction {
    Object consts;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__2 = RT.var((String)"datomic.datalog", (String)"variable?");

    public query$process_ranges$const_QMARK___19321(Object object) {
        this.consts = object;
    }

    public Object invoke(Object p1__19319_SHARP_) {
        Object object;
        Object or__5238__auto__19323;
        Object object2 = or__5238__auto__19323 = RT.get((Object)this_.consts, (Object)p1__19319_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__19323;
            or__5238__auto__19323 = null;
        } else {
            Object object3 = p1__19319_SHARP_;
            p1__19319_SHARP_ = null;
            query$process_ranges$const_QMARK___19321 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object3));
        }
        return object;
    }
}

