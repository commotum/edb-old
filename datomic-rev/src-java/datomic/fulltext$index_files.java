/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.fulltext$index_files$fn__14541;

public final class fulltext$index_files
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"file-seq");

    public static Object invokeStatic(Object index_dir) {
        Object object = index_dir;
        index_dir = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new fulltext$index_files$fn__14541(), (Object)PersistentArrayMap.EMPTY, ((IFn)const__1.getRawRoot()).invoke(object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fulltext$index_files.invokeStatic(object2);
    }
}

