/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.datomic.lucene.store.FSDirectory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datomic.lucene.store.FSDirectory;
import java.io.File;

public final class lucene$fs_directory
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.java.io", (String)"file");

    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return FSDirectory.open((File)((File)((IFn)const__0.getRawRoot()).invoke(object)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return lucene$fs_directory.invokeStatic(object2);
    }
}

