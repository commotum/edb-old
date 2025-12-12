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
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.List;

public final class datalog$binding_type
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final Keyword const__2 = RT.keyword(null, (String)"scalar");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"rel");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"...");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"second");
    public static final Keyword const__10 = RT.keyword(null, (String)"list");
    public static final Keyword const__11 = RT.keyword(null, (String)"else");
    public static final Keyword const__12 = RT.keyword(null, (String)"tuple");
    public static final Var const__13 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__14 = RT.keyword((String)"db.error", (String)"not-a-binding-form");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object binds) {
        Object object;
        if (Util.identical((Object)binds, null)) {
            object = null;
        } else {
            Object object2 = ((IFn)const__1.getRawRoot()).invoke(binds);
            if (object2 != null && object2 != Boolean.FALSE) {
                object = const__2;
            } else if (binds instanceof List) {
                if (((IFn)const__5.getRawRoot()).invoke(binds) instanceof List) {
                    object = const__6;
                } else {
                    Object object3 = binds;
                    binds = null;
                    if (Util.equiv((Object)const__8, (Object)((IFn)const__9.getRawRoot()).invoke(object3))) {
                        object = const__10;
                    } else {
                        Keyword keyword = const__11;
                        object = keyword != null && keyword != Boolean.FALSE ? const__12 : null;
                    }
                }
            } else {
                Keyword keyword = const__11;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object object4 = binds;
                    binds = null;
                    object = ((IFn)const__13.getRawRoot()).invoke((Object)const__14, ((IFn)const__15.getRawRoot()).invoke((Object)"Invalid binding form: ", object4));
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
        return datalog$binding_type.invokeStatic(object2);
    }
}

