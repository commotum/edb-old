/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.qtune$min_ret$fn__23441;
import datomic.qtune$min_ret$fn__23445;

public final class qtune$min_ret
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"prn");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"min-key");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"println");

    public static Object invokeStatic(Object qs, Object args, Object timeout) {
        Object object;
        block2: {
            Object ret;
            while (true) {
                if (1L == (long)RT.count((Object)qs)) {
                    Object vec__23438;
                    Object object2 = qs;
                    qs = null;
                    Object object3 = vec__23438 = ((IFn)const__3.getRawRoot()).invoke(object2);
                    vec__23438 = null;
                    Object clause = RT.nth((Object)object3, (int)RT.intCast((long)0L), null);
                    ((IFn)const__6.getRawRoot()).invoke(clause);
                    object = clause;
                    clause = null;
                    break block2;
                }
                ret = ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), (Object)new qtune$min_ret$fn__23441(), ((IFn)const__9.getRawRoot()).invoke((Object)new qtune$min_ret$fn__23445(args, timeout), qs));
                if (!Util.identical((Object)((IFn)const__11.getRawRoot()).invoke(ret), null)) break;
                Object object4 = timeout;
                timeout = null;
                Number timeout2 = Numbers.multiply((long)2L, (Object)object4);
                ((IFn)const__14.getRawRoot()).invoke((Object)"Retrying with timeout: ", (Object)timeout2, (Object)" milliseconds");
                Object object5 = qs;
                qs = null;
                Object object6 = args;
                args = null;
                Number number = timeout2;
                timeout2 = null;
                timeout = number;
                args = object6;
                qs = object5;
            }
            Object object7 = ret;
            ret = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object7);
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return qtune$min_ret.invokeStatic(object4, object5, object6);
    }
}

