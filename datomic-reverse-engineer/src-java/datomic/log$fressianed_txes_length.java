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
import datomic.log$fressianed_txes_length$fn__16135;

public final class log$fressianed_txes_length
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object fressianed_txes) {
        Object object = fressianed_txes;
        fressianed_txes = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke((Object)new log$fressianed_txes_length$fn__16135(), object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$fressianed_txes_length.invokeStatic(object2);
    }
}

