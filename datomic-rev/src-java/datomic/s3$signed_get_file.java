/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.java.io.Coercions
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.java.io.Coercions;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.net.URL;

public final class s3$signed_get_file
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object url, Object f) {
        v0 = url;
        url = null;
        is = ((URL)v0).openStream();
        try {
            v1 = (IFn)s3$signed_get_file.const__0.getRawRoot();
            v2 = f;
            f = null;
            v3 = v2;
            if (Util.classOf((Object)v2) == s3$signed_get_file.__cached_class__0) ** GOTO lbl13
            if (!(v3 instanceof Coercions)) {
                v3 = v3;
                s3$signed_get_file.__cached_class__0 = Util.classOf((Object)v3);
lbl13:
                // 2 sources

                v4 = s3$signed_get_file.const__1.getRawRoot().invoke(v3);
            } else {
                v4 = ((Coercions)v3).as_file();
            }
            var3_3 = v1.invoke((Object)is, v4);
        }
        finally {
            v5 = is;
            is = null;
            v5.close();
        }
        return var3_3;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3$signed_get_file.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"clojure.java.io", (String)"copy");
        const__1 = RT.var((String)"clojure.java.io", (String)"as-file");
    }
}

