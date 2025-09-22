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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class datalog$not_or__GT_not_or_join
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final AFn const__3 = (AFn)PersistentHashSet.create((Object[])new Object[]{Symbol.intern(null, (String)"or"), Symbol.intern(null, (String)"not")});
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__5 = RT.var((String)"datomic.datalog", (String)"unifying-var-set");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"list*");
    public static final AFn const__8 = (AFn)RT.map((Object[])new Object[]{Symbol.intern(null, (String)"not"), Symbol.intern(null, (String)"not-join"), Symbol.intern(null, (String)"or"), Symbol.intern(null, (String)"or-join")});
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"meta");

    public static Object invokeStatic(Object p__18701) {
        Object object;
        Object object2 = p__18701;
        p__18701 = null;
        Object vec__18702 = object2;
        Object seq__18703 = ((IFn)const__0.getRawRoot()).invoke(vec__18702);
        Object first__18704 = ((IFn)const__1.getRawRoot()).invoke(seq__18703);
        Object object3 = seq__18703;
        seq__18703 = null;
        Object seq__187032 = ((IFn)const__2.getRawRoot()).invoke(object3);
        Object object4 = first__18704;
        first__18704 = null;
        Object p = object4;
        Object object5 = seq__187032;
        seq__187032 = null;
        Object cs = object5;
        Object object6 = vec__18702;
        vec__18702 = null;
        Object c = object6;
        Object object7 = ((IFn)const__3).invoke(p);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object vs = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(c));
            Object object8 = p;
            p = null;
            Object object9 = vs;
            vs = null;
            Object object10 = cs;
            cs = null;
            Object object11 = c;
            c = null;
            object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8).invoke(object8), object9, object10), ((IFn)const__9.getRawRoot()).invoke(object11));
        } else {
            object = c;
            c = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$not_or__GT_not_or_join.invokeStatic(object2);
    }
}

