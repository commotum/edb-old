/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog$sched_in_order$underbound_QMARK___18522$fn__18523;
import datomic.datalog$sched_in_order$underbound_QMARK___18522$fn__18526;
import datomic.datalog$sched_in_order$underbound_QMARK___18522$fn__18528;
import datomic.datalog$sched_in_order$underbound_QMARK___18522$fn__18530;

public final class datalog$sched_in_order$underbound_QMARK___18522
extends AFunction {
    Object unpack;
    Object extdb;
    Object reqcnt;
    Object prog;
    Object hpred;
    Object cargs;
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"not-join-clause?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"take");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__10 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Keyword const__11 = RT.keyword(null, (String)"else");

    public datalog$sched_in_order$underbound_QMARK___18522(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.unpack = object;
        this.extdb = object2;
        this.reqcnt = object3;
        this.prog = object4;
        this.hpred = object5;
        this.cargs = object6;
    }

    public Object invoke(Object n, Object bindings, Object pc) {
        Object object;
        datalog$sched_in_order$underbound_QMARK___18522 this_;
        Object object2;
        Object or__5238__auto__18534;
        Object clause = ((IFn)this_.unpack).invoke(pc);
        Object args = ((IFn)this_.cargs).invoke(clause);
        Object object3 = or__5238__auto__18534 = ((IFn)const__0.getRawRoot()).invoke(clause);
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = or__5238__auto__18534;
            or__5238__auto__18534 = null;
        } else {
            object2 = ((IFn)const__1.getRawRoot()).invoke(clause);
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object4 = bindings;
            bindings = null;
            Object object5 = args;
            args = null;
            this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)new datalog$sched_in_order$underbound_QMARK___18522$fn__18523(object4), object5);
        } else {
            Object object6 = ((IFn)const__3.getRawRoot()).invoke(this_.prog, ((IFn)const__4.getRawRoot()).invoke(clause));
            if (object6 != null && object6 != Boolean.FALSE) {
                Object or__5238__auto__18537;
                Object object7;
                Object rcnt;
                Object and__5236__auto__18535;
                Object object8 = and__5236__auto__18535 = (rcnt = ((IFn)this_.reqcnt).invoke(clause));
                if (object8 != null && object8 != Boolean.FALSE) {
                    Object object9 = rcnt;
                    rcnt = null;
                    object7 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)new datalog$sched_in_order$underbound_QMARK___18522$fn__18526(bindings), ((IFn)const__7.getRawRoot()).invoke(object9, args)));
                } else {
                    object7 = and__5236__auto__18535;
                    and__5236__auto__18535 = null;
                }
                Object object10 = or__5238__auto__18537 = object7;
                if (object10 != null && object10 != Boolean.FALSE) {
                    object = or__5238__auto__18537;
                    or__5238__auto__18537 = null;
                } else {
                    Object object11 = clause;
                    clause = null;
                    boolean and__5236__auto__18536 = Util.equiv((Object)this_.hpred, (Object)((IFn)const__4.getRawRoot()).invoke(object11));
                    if (and__5236__auto__18536) {
                        Object object12 = bindings;
                        bindings = null;
                        Object object13 = args;
                        args = null;
                        this_ = null;
                        object = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)new datalog$sched_in_order$underbound_QMARK___18522$fn__18528(object12), ((IFn)const__9.getRawRoot()).invoke(const__10.getRawRoot(), object13)));
                    } else {
                        object = and__5236__auto__18536 ? Boolean.TRUE : Boolean.FALSE;
                    }
                }
            } else {
                Keyword keyword = const__11;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object temp__5457__auto__18538;
                    Object object14 = pc;
                    pc = null;
                    Object object15 = temp__5457__auto__18538 = ((IFn)this_.extdb).invoke(object14);
                    if (object15 != null && object15 != Boolean.FALSE) {
                        temp__5457__auto__18538 = null;
                        Object object16 = bindings;
                        bindings = null;
                        Object object17 = n;
                        n = null;
                        Object object18 = clause;
                        clause = null;
                        this_ = null;
                        object = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)new datalog$sched_in_order$underbound_QMARK___18522$fn__18530(object16), ((IFn)const__7.getRawRoot()).invoke(object17, object18)));
                    } else {
                        object = null;
                    }
                } else {
                    object = null;
                }
            }
        }
        return object;
    }
}

