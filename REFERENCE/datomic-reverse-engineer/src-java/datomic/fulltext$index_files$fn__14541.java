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
import java.io.File;

public final class fulltext$index_files$fn__14541
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");

    public Object invoke(Object m, Object f) {
        Object object;
        if (((File)f).isFile()) {
            Object object2 = m;
            m = null;
            Object object3 = f;
            Object object4 = f;
            f = null;
            fulltext$index_files$fn__14541 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object2, object3, (Object)((File)object4).getName());
        } else {
            object = m;
            Object var1_1 = null;
        }
        return object;
    }
}

