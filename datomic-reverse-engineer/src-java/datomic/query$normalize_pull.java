/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class query$normalize_pull
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"source?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"first");
    public static final AFn const__4 = (AFn)Symbol.intern(null, (String)"$");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"rest");
    public static final Var const__6 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__9 = RT.var((String)"datomic.query", (String)"pattern?");
    public static final Var const__11 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__12 = RT.keyword((String)"db.error", (String)"invalid-pull");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object expr) {
        Object object;
        Object and__5236__auto__19378;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(expr));
        Object result2 = object2 != null && object2 != Boolean.FALSE ? expr : ((IFn)const__2.getRawRoot()).invoke((Object)Tuple.create((Object)((IFn)const__3.getRawRoot()).invoke(expr), (Object)const__4), ((IFn)const__5.getRawRoot()).invoke(expr));
        Object object3 = and__5236__auto__19378 = ((IFn)const__6.getRawRoot()).invoke(RT.nth((Object)result2, (int)RT.intCast((long)2L)));
        if (object3 != null && object3 != Boolean.FALSE) {
            object = ((IFn)const__9.getRawRoot()).invoke(RT.nth((Object)result2, (int)RT.intCast((long)3L)));
        } else {
            object = and__5236__auto__19378;
            Object var2_2 = null;
        }
        if (object != null && object != Boolean.FALSE) {
        } else {
            Object object4 = expr;
            expr = null;
            ((IFn)const__11.getRawRoot()).invoke((Object)const__12, ((IFn)const__13.getRawRoot()).invoke((Object)"Invalid pull expression ", object4));
        }
        Object var1_1 = null;
        return result2;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$normalize_pull.invokeStatic(object2);
    }
}

