/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.tools.index_checks$unique_collisions$fn__21905;

public final class index_checks$unique_collisions
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__2 = RT.var((String)"datomic.tools.index-checks", (String)"non-unique?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"datomic.tools.index-checks", (String)"reporter");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"partition");
    public static final Object const__6 = 2L;
    public static final Object const__7 = 1L;
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__10 = RT.var((String)"datomic.tools", (String)"unique-identities");
    public static final Var const__11 = RT.var((String)"datomic.tools", (String)"unique-values");

    public static Object invokeStatic(Object db2, Object progress) {
        Object object = progress;
        progress = null;
        index_checks$unique_collisions$fn__21905 index_checks$unique_collisions$fn__21905 = new index_checks$unique_collisions$fn__21905(db2);
        Object object2 = ((IFn)const__10.getRawRoot()).invoke(db2);
        Object object3 = db2;
        db2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object), ((IFn)const__5.getRawRoot()).invoke(const__6, const__7, ((IFn)const__8.getRawRoot()).invoke((Object)index_checks$unique_collisions$fn__21905, ((IFn)const__9.getRawRoot()).invoke(object2, ((IFn)const__11.getRawRoot()).invoke(object3)))))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index_checks$unique_collisions.invokeStatic(object3, object4);
    }
}

