/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

public final class valcache$mkdirs
extends AFunction {
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__5 = RT.classForName((String)"java.lang.String");
    public static final Var const__6 = RT.var((String)"datomic.valcache", (String)"dirname");
    public static final Object const__7 = RT.classForName((String)"java.nio.file.attribute.FileAttribute");

    public static Object invokeStatic(Object root) {
        long n__5742__auto__9702 = 4096L;
        for (long n = 0L; n < n__5742__auto__9702; ++n) {
            Path path2;
            Path path3 = path2 = FileSystems.getDefault().getPath((String)root, (String[])((IFn)const__4.getRawRoot()).invoke(const__5, (Object)Tuple.create((Object)((IFn)const__6.getRawRoot()).invoke((Object)Numbers.num((long)n)))));
            path2 = null;
            Files.createDirectories(path3, (FileAttribute[])((IFn)const__4.getRawRoot()).invoke(const__7, (Object)PersistentVector.EMPTY));
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache$mkdirs.invokeStatic(object2);
    }
}

