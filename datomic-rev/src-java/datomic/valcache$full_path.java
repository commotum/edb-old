/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public final class valcache$full_path
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.valcache", (String)"uuid-prefix");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__2 = RT.classForName((String)"java.lang.String");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"subs");

    public static Object invokeStatic(Object root, Object k) {
        Path path2;
        Object temp__5457__auto__9665;
        Object object = temp__5457__auto__9665 = ((IFn)const__0.getRawRoot()).invoke(k);
        if (object != null && object != Boolean.FALSE) {
            Object object2 = temp__5457__auto__9665;
            temp__5457__auto__9665 = null;
            Object uuid_prefix2 = object2;
            Object object3 = root;
            root = null;
            Object object4 = uuid_prefix2;
            Object object5 = uuid_prefix2;
            uuid_prefix2 = null;
            Object object6 = k;
            k = null;
            path2 = FileSystems.getDefault().getPath((String)object3, (String[])((IFn)const__1.getRawRoot()).invoke(const__2, (Object)Tuple.create((Object)((IFn)const__3.getRawRoot()).invoke(object4, (Object)Numbers.num((long)Numbers.minus((long)RT.count((Object)object5), (long)3L))), (Object)object6)));
        } else {
            path2 = null;
        }
        return path2;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return valcache$full_path.invokeStatic(object3, object4);
    }
}

