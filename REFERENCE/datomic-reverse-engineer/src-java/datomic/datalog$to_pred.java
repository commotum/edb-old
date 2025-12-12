/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
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
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;

public final class datalog$to_pred
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final AFn const__3 = (AFn)PersistentHashSet.create((Object[])new Object[]{Symbol.intern(null, (String)"or"), Symbol.intern(null, (String)"and")});
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__6 = RT.var((String)"datomic.datalog", (String)"to-pred");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"not");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"list");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"list*");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"and");
    public static final Keyword const__12 = RT.keyword(null, (String)"else");

    public static Object invokeStatic(Object p__18690) {
        Object object;
        Object object2 = p__18690;
        p__18690 = null;
        Object vec__18691 = object2;
        Object seq__18692 = ((IFn)const__0.getRawRoot()).invoke(vec__18691);
        Object first__18693 = ((IFn)const__1.getRawRoot()).invoke(seq__18692);
        Object object3 = seq__18692;
        seq__18692 = null;
        Object seq__186922 = ((IFn)const__2.getRawRoot()).invoke(object3);
        Object object4 = first__18693;
        first__18693 = null;
        Object p = object4;
        Object object5 = seq__186922;
        seq__186922 = null;
        Object cs = object5;
        vec__18691 = null;
        Object object6 = ((IFn)const__3).invoke(p);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = p;
            p = null;
            Object object8 = cs;
            cs = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object7, ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot(), object8));
        } else if (Util.equiv((Object)const__8, (Object)p)) {
            Object object9 = p;
            p = null;
            Object object10 = cs;
            cs = null;
            object = ((IFn)const__9.getRawRoot()).invoke(object9, ((IFn)const__10.getRawRoot()).invoke((Object)const__11, ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot(), object10)));
        } else {
            Keyword keyword = const__12;
            if (keyword != null && keyword != Boolean.FALSE) {
                object = p;
                p = null;
            } else {
                object = null;
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$to_pred.invokeStatic(object2);
    }
}

