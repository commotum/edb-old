/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.external_sort;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.external_sort.FileSystemSorter$fn__14469$fn__14470$fn__14471$fn__14472;

public final class FileSystemSorter$fn__14469$fn__14470$fn__14471
extends AFunction {
    Object write;
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"reduce");

    public FileSystemSorter$fn__14469$fn__14470$fn__14471(Object object) {
        this.write = object;
    }

    public Object invoke(Object p1__14458_SHARP_) {
        Object object = p1__14458_SHARP_;
        p1__14458_SHARP_ = null;
        FileSystemSorter$fn__14469$fn__14470$fn__14471 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new FileSystemSorter$fn__14469$fn__14470$fn__14471$fn__14472(this_.write), null, object);
    }
}

