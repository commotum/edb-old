/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class datalog$add_rule$fn__18746
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__6 = RT.var((String)"datomic.datalog", (String)"prep-clauses");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"flatten");

    public Object invoke(Object p__18744, Object p__18745) {
        Object object = p__18744;
        p__18744 = null;
        Object vec__18747 = object;
        Object rm = RT.nth((Object)vec__18747, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__18747;
        vec__18747 = null;
        Object ps = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        Object object3 = p__18745;
        p__18745 = null;
        Object vec__18750 = object3;
        Object seq__18751 = ((IFn)const__3.getRawRoot()).invoke(vec__18750);
        Object first__18752 = ((IFn)const__4.getRawRoot()).invoke(seq__18751);
        Object object4 = seq__18751;
        seq__18751 = null;
        Object seq__187512 = ((IFn)const__5.getRawRoot()).invoke(object4);
        Object object5 = first__18752;
        first__18752 = null;
        Object head = object5;
        Object object6 = seq__187512;
        seq__187512 = null;
        Object clauses = object6;
        vec__18750 = null;
        Object object7 = rm;
        rm = null;
        Object object8 = clauses;
        clauses = null;
        Object vec__18753 = ((IFn)const__6.getRawRoot()).invoke(object7, object8);
        Object rm2 = RT.nth((Object)vec__18753, (int)RT.uncheckedIntCast((long)0L), null);
        Object object9 = vec__18753;
        vec__18753 = null;
        Object cs = RT.nth((Object)object9, (int)RT.uncheckedIntCast((long)1L), null);
        Object object10 = rm2;
        rm2 = null;
        Object object11 = ps;
        ps = null;
        Object object12 = head;
        head = null;
        Object object13 = cs;
        cs = null;
        return Tuple.create((Object)object10, (Object)((IFn)const__7.getRawRoot()).invoke(object11, ((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(object12)), object13)));
    }
}

