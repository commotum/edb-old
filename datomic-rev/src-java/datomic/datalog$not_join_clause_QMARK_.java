/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.List;

public final class datalog$not_join_clause_QMARK_
extends AFunction {
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"not-join");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");

    public static Object invokeStatic(Object c) {
        Boolean bl;
        boolean and__5236__auto__18427 = c instanceof List;
        if (and__5236__auto__18427) {
            Object object = c;
            c = null;
            bl = Util.equiv((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke(object)) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = and__5236__auto__18427 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$not_join_clause_QMARK_.invokeStatic(object2);
    }
}

