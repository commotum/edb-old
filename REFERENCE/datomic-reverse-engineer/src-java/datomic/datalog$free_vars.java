/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datalog$free_vars
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"not-join-clause?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"second");
    public static final Keyword const__2 = RT.keyword(null, (String)"else");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__4 = RT.var((String)"datomic.datalog", (String)"free-var?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"flatten");

    public static Object invokeStatic(Object c) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(c);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = c;
            c = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3);
        } else {
            Keyword keyword = const__2;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object object4 = c;
                c = null;
                object = ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(object4));
            } else {
                object = null;
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$free_vars.invokeStatic(object2);
    }
}

