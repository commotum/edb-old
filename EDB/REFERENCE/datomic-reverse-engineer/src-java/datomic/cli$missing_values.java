/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class cli$missing_values
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__2 = RT.var((String)"datomic.cli", (String)"unique-index");
    public static final Keyword const__3 = RT.keyword(null, (String)"long-name");
    public static final Keyword const__4 = RT.keyword(null, (String)"required");
    public static final Var const__5 = RT.var((String)"clojure.set", (String)"difference");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"set");

    public static Object invokeStatic(Object m, Object spec) {
        Object required;
        Object object = spec;
        spec = null;
        Object object2 = required = ((IFn)const__0.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object, (Object)const__3, (Object)const__4)));
        required = null;
        Object object3 = m;
        m = null;
        return ((IFn)const__5.getRawRoot()).invoke(object2, ((IFn)const__6.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object3)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return cli$missing_values.invokeStatic(object3, object4);
    }
}

