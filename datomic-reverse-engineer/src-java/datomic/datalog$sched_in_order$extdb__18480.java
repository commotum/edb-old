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
import datomic.db.IDb;

public final class datalog$sched_in_order$extdb__18480
extends AFunction {
    Object prog;
    Object srcs;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__5 = RT.var((String)"datomic.datalog", (String)"not-join-clause?");
    public static final Var const__6 = RT.var((String)"datomic.datalog", (String)"extensional?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"first");

    public datalog$sched_in_order$extdb__18480(Object object, Object object2) {
        this.prog = object;
        this.srcs = object2;
    }

    public Object invoke(Object p__18479) {
        Object object;
        Object temp__5457__auto__18488;
        Object object2;
        Object and__5236__auto__18487;
        Object object3 = p__18479;
        p__18479 = null;
        Object vec__18481 = object3;
        Object src = RT.nth((Object)vec__18481, (int)RT.uncheckedIntCast((long)0L), null);
        Object object4 = vec__18481;
        vec__18481 = null;
        Object c = RT.nth((Object)object4, (int)RT.uncheckedIntCast((long)1L), null);
        Object object5 = and__5236__auto__18487 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(c));
        if (object5 != null && object5 != Boolean.FALSE) {
            Object and__5236__auto__18486;
            Object object6 = and__5236__auto__18486 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(c));
            if (object6 != null && object6 != Boolean.FALSE) {
                Object and__5236__auto__18485;
                Object object7 = c;
                c = null;
                Object object8 = and__5236__auto__18485 = ((IFn)const__6.getRawRoot()).invoke(this.prog, ((IFn)const__7.getRawRoot()).invoke(object7));
                if (object8 != null && object8 != Boolean.FALSE) {
                    Object object9 = src;
                    src = null;
                    object2 = ((IFn)this.srcs).invoke(object9);
                } else {
                    object2 = and__5236__auto__18485;
                    and__5236__auto__18485 = null;
                }
            } else {
                object2 = and__5236__auto__18486;
                and__5236__auto__18486 = null;
            }
        } else {
            object2 = and__5236__auto__18487;
            and__5236__auto__18487 = null;
        }
        Object object10 = temp__5457__auto__18488 = object2;
        if (object10 != null && object10 != Boolean.FALSE) {
            Object object11 = temp__5457__auto__18488;
            temp__5457__auto__18488 = null;
            Object db2 = object11;
            if (db2 instanceof IDb) {
                object = db2;
                db2 = null;
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return object;
    }
}

