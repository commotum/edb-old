/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic.external_sort;

import clojure.lang.AFunction;
import clojure.lang.IFn;

public final class FileSystemSorter$fn__14469$fn__14470$fn__14471$fn__14472
extends AFunction {
    Object write;

    public FileSystemSorter$fn__14469$fn__14470$fn__14471$fn__14472(Object object) {
        this.write = object;
    }

    public Object invoke(Object _, Object item) {
        Object object = item;
        item = null;
        FileSystemSorter$fn__14469$fn__14470$fn__14471$fn__14472 this_ = null;
        return ((IFn)this_.write).invoke(object);
    }
}

