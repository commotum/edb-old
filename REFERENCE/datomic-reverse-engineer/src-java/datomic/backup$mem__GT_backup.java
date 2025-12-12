/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class backup$mem__GT_backup
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.set", (String)"rename-keys");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"require-keys");
    public static final Var const__2 = RT.var((String)"datomic.backup", (String)"mem-keys");
    public static final Var const__3 = RT.var((String)"datomic.backup", (String)"v1->v2-keymap");

    public static Object invokeStatic(Object roots) {
        Object object = roots;
        roots = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, const__2.getRawRoot()), const__3.getRawRoot());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$mem__GT_backup.invokeStatic(object2);
    }
}

