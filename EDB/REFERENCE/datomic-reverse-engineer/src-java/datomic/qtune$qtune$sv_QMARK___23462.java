/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.List;

public final class qtune$qtune$sv_QMARK___23462
extends AFunction {
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__10 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)"...");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"second");

    public Object invoke(Object p__23461) {
        Object object;
        Object object2 = p__23461;
        p__23461 = null;
        Object vec__23463 = object2;
        Object call = RT.nth((Object)vec__23463, (int)RT.intCast((long)0L), null);
        Object object3 = vec__23463;
        vec__23463 = null;
        Object binds = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        if (call instanceof List) {
            Object and__5236__auto__23472;
            Object vec__23466;
            Object object4 = call;
            call = null;
            Object object5 = vec__23466 = object4;
            vec__23466 = null;
            Object seq__23467 = ((IFn)const__5.getRawRoot()).invoke(object5);
            Object first__23468 = ((IFn)const__6.getRawRoot()).invoke(seq__23467);
            Object object6 = seq__23467;
            seq__23467 = null;
            Object seq__234672 = ((IFn)const__7.getRawRoot()).invoke(object6);
            first__23468 = null;
            Object object7 = seq__234672;
            seq__234672 = null;
            Object body = object7;
            Object object8 = and__5236__auto__23472 = binds;
            if (object8 != null && object8 != Boolean.FALSE) {
                Object object9 = body;
                body = null;
                boolean and__5236__auto__23471 = Util.identical((Object)((IFn)const__5.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(const__10.getRawRoot(), object9)), null);
                if (and__5236__auto__23471) {
                    Object or__5238__auto__23470;
                    Object object10 = or__5238__auto__23470 = ((IFn)const__11.getRawRoot()).invoke(binds);
                    if (object10 != null && object10 != Boolean.FALSE) {
                        object = or__5238__auto__23470;
                        or__5238__auto__23470 = null;
                    } else {
                        Object object11 = binds;
                        binds = null;
                        qtune$qtune$sv_QMARK___23462 this_ = null;
                        object = Util.equiv((Object)const__13, (Object)((IFn)const__14.getRawRoot()).invoke(object11)) ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    object = and__5236__auto__23471 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object = and__5236__auto__23472;
                and__5236__auto__23472 = null;
            }
        } else {
            object = null;
        }
        return object;
    }
}

