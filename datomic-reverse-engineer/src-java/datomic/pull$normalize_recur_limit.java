/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class pull$normalize_recur_limit
extends AFunction {
    public static final AFn const__1 = (AFn)PersistentHashSet.create((Object[])new Object[]{"...", Symbol.intern(null, (String)"...")});
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)"...");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Keyword const__6 = RT.keyword(null, (String)"default");
    public static final Var const__7 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__8 = RT.keyword((String)"db.error", (String)"invalid-recur-limit");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object x) {
        Object object;
        Object object2 = ((IFn)const__1).invoke(x);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = const__2;
        } else {
            Object object3;
            Object and__5236__auto__18967;
            Object object4 = and__5236__auto__18967 = ((IFn)const__3.getRawRoot()).invoke(x);
            if (object4 != null && object4 != Boolean.FALSE) {
                object3 = Numbers.lt((long)0L, (Object)x) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object3 = and__5236__auto__18967;
                Object var1_1 = null;
            }
            if (object3 != null && object3 != Boolean.FALSE) {
                object = x;
                x = null;
            } else {
                Keyword keyword = const__6;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object object5 = x;
                    x = null;
                    object = ((IFn)const__7.getRawRoot()).invoke((Object)const__8, ((IFn)const__9.getRawRoot()).invoke((Object)"Cannot interpret as a recursive pull specification: ", object5));
                } else {
                    object = null;
                }
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return pull$normalize_recur_limit.invokeStatic(object2);
    }
}

