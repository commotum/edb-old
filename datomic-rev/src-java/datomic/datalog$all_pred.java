/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.List;

public final class datalog$all_pred
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final AFn const__3 = (AFn)PersistentHashSet.create((Object[])new Object[]{Symbol.intern(null, (String)"or"), Symbol.intern(null, (String)"not"), Symbol.intern(null, (String)"and")});
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__5 = RT.var((String)"datomic.datalog", (String)"all-pred");

    public static Object invokeStatic(Object p__18684) {
        Object object;
        Object object2 = p__18684;
        p__18684 = null;
        Object vec__18685 = object2;
        Object seq__18686 = ((IFn)const__0.getRawRoot()).invoke(vec__18685);
        Object first__18687 = ((IFn)const__1.getRawRoot()).invoke(seq__18686);
        Object object3 = seq__18686;
        seq__18686 = null;
        Object seq__186862 = ((IFn)const__2.getRawRoot()).invoke(object3);
        Object object4 = first__18687;
        first__18687 = null;
        Object p = object4;
        Object object5 = seq__186862;
        seq__186862 = null;
        Object cs = object5;
        vec__18685 = null;
        Object object6 = ((IFn)const__3).invoke(p);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = cs;
            cs = null;
            object = ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), object7);
        } else {
            boolean bl;
            Object object8 = p;
            p = null;
            boolean and__5236__auto__18689 = object8 instanceof List;
            if (and__5236__auto__18689) {
                Object object9 = cs;
                cs = null;
                bl = Util.identical((Object)object9, null);
            } else {
                bl = and__5236__auto__18689;
            }
            object = bl ? Boolean.TRUE : null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$all_pred.invokeStatic(object2);
    }
}

