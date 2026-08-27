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

public final class ddb$fullname
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"keyword?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"subs");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Object const__3 = 1L;
    public static final Keyword const__4 = RT.keyword(null, (String)"default");

    public static Object invokeStatic(Object s) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(s);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = s;
            s = null;
            object = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object3), const__3);
        } else {
            Keyword keyword = const__4;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object object4 = s;
                s = null;
                object = ((IFn)const__2.getRawRoot()).invoke(object4);
            } else {
                object = null;
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$fullname.invokeStatic(object2);
    }
}

