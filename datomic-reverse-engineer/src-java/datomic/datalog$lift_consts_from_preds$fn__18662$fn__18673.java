/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class datalog$lift_consts_from_preds$fn__18662$fn__18673
extends AFunction {
    public static final Var const__3 = RT.var((String)"datomic.datalog", (String)"variable-or-blank?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"gensym");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");

    public Object invoke(Object p__18672, Object arg2) {
        IPersistentVector iPersistentVector;
        Object object = p__18672;
        p__18672 = null;
        Object vec__18674 = object;
        Object m = RT.nth((Object)vec__18674, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__18674;
        vec__18674 = null;
        Object args = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        Object object3 = ((IFn)const__3.getRawRoot()).invoke(arg2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = m;
            m = null;
            Object object5 = args;
            args = null;
            Object object6 = arg2;
            arg2 = null;
            iPersistentVector = Tuple.create((Object)object4, (Object)((IFn)const__4.getRawRoot()).invoke(object5, object6));
        } else {
            Object garg = ((IFn)const__5.getRawRoot()).invoke((Object)"?c__");
            Object object7 = m;
            m = null;
            Object object8 = arg2;
            arg2 = null;
            Object object9 = ((IFn)const__6.getRawRoot()).invoke(object7, garg, object8);
            Object object10 = args;
            args = null;
            Object object11 = garg;
            garg = null;
            iPersistentVector = Tuple.create((Object)object9, (Object)((IFn)const__4.getRawRoot()).invoke(object10, object11));
        }
        return iPersistentVector;
    }
}

