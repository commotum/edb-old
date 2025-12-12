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

public final class common$pop_atom_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"compare-and-set!");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"rest");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");

    public static Object invokeStatic(Object atm) {
        Object object;
        block2: {
            Object xs = ((IFn)const__0.getRawRoot()).invoke(atm);
            while (true) {
                Object object2 = ((IFn)const__1.getRawRoot()).invoke(xs);
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object object3 = ((IFn)const__2.getRawRoot()).invoke(atm, xs, ((IFn)const__3.getRawRoot()).invoke(xs));
                if (object3 != null && object3 != Boolean.FALSE) {
                    Object object4 = xs;
                    xs = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object4);
                    break block2;
                }
                xs = ((IFn)const__0.getRawRoot()).invoke(atm);
            }
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$pop_atom_BANG_.invokeStatic(object2);
    }
}

