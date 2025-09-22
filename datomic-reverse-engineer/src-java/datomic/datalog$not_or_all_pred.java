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

public final class datalog$not_or_all_pred
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final AFn const__3 = (AFn)PersistentHashSet.create((Object[])new Object[]{Symbol.intern(null, (String)"or"), Symbol.intern(null, (String)"not")});
    public static final Var const__4 = RT.var((String)"datomic.datalog", (String)"all-pred");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__6 = RT.var((String)"datomic.datalog", (String)"unifying-var-set");
    public static final Var const__7 = RT.var((String)"datomic.datalog", (String)"to-pred");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"vector");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__12 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");

    public static Object invokeStatic(Object p__18695) {
        Object object;
        Object object2;
        Object and__5236__auto__18700;
        Object object3 = p__18695;
        p__18695 = null;
        Object vec__18696 = object3;
        Object seq__18697 = ((IFn)const__0.getRawRoot()).invoke(vec__18696);
        Object first__18698 = ((IFn)const__1.getRawRoot()).invoke(seq__18697);
        Object object4 = seq__18697;
        seq__18697 = null;
        Object seq__186972 = ((IFn)const__2.getRawRoot()).invoke(object4);
        Object object5 = first__18698;
        first__18698 = null;
        Object p = object5;
        seq__186972 = null;
        Object object6 = vec__18696;
        vec__18696 = null;
        Object c = object6;
        Object object7 = p;
        p = null;
        Object object8 = and__5236__auto__18700 = ((IFn)const__3).invoke(object7);
        if (object8 != null && object8 != Boolean.FALSE) {
            object2 = ((IFn)const__4.getRawRoot()).invoke(c);
        } else {
            object2 = and__5236__auto__18700;
            and__5236__auto__18700 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object pred2;
            Object vs = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(c));
            Object object9 = c;
            c = null;
            Object object10 = pred2 = ((IFn)const__7.getRawRoot()).invoke(object9);
            pred2 = null;
            Object object11 = ((IFn)const__11.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke((Object)const__12), ((IFn)const__11.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(vs)))), ((IFn)const__11.getRawRoot()).invoke(object10))));
            Object object12 = vs;
            vs = null;
            object = ((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(object11, object12))))));
        } else {
            object = c;
            c = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$not_or_all_pred.invokeStatic(object2);
    }
}

