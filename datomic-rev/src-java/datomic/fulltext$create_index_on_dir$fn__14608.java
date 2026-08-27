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
import java.io.Closeable;

public final class fulltext$create_index_on_dir$fn__14608
extends AFunction {
    Object remove_data;
    Object reader;
    public static final Var const__0 = RT.var((String)"datomic.fulltext", (String)"remove-data-from-reader");

    public fulltext$create_index_on_dir$fn__14608(Object object, Object object2) {
        this.remove_data = object;
        this.reader = object2;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.reader, this.remove_data);
        }
        finally {
            ((Closeable)this.reader).close();
        }
        return object;
    }
}

