/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class Db$fn__13475
extends AFunction {
    Object it;
    boolean card_one_QMARK_;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"retracts?");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"updates-v?");

    public Db$fn__13475(Object object, boolean bl) {
        this.it = object;
        this.card_one_QMARK_ = bl;
    }

    public Object invoke(Object p1__13419_SHARP_) {
        Object object;
        Object or__5238__auto__13478;
        Object object2 = or__5238__auto__13478 = ((IFn)const__0.getRawRoot()).invoke(p1__13419_SHARP_, this_.it);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__13478;
            or__5238__auto__13478 = null;
        } else {
            boolean and__5236__auto__13477 = this_.card_one_QMARK_;
            if (and__5236__auto__13477) {
                Object object3 = p1__13419_SHARP_;
                p1__13419_SHARP_ = null;
                Db$fn__13475 this_ = null;
                object = ((IFn)const__1.getRawRoot()).invoke(object3, this_.it);
            } else {
                object = and__5236__auto__13477 ? Boolean.TRUE : Boolean.FALSE;
            }
        }
        return object;
    }
}

