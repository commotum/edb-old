/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.java.io.Coercions
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.java.io.Coercions;
import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.net.URL;

public final class aws_detect$quickstream
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object path, Object timeout) {
        v0 = path;
        path = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == aws_detect$quickstream.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof Coercions)) {
            v1 = v1;
            aws_detect$quickstream.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = aws_detect$quickstream.const__0.getRawRoot().invoke(v1);
        } else {
            v2 = ((Coercions)v1).as_url();
        }
        v3 = url = v2;
        url = null;
        G__20965 = ((URL)v3).openConnection();
        G__20965.setConnectTimeout(RT.intCast((Object)((Number)timeout)));
        v4 = timeout;
        timeout = null;
        G__20965.setReadTimeout(RT.intCast((Object)((Number)v4)));
        conn = null;
        v5 = conn = G__20965;
        conn = null;
        return v5.getInputStream();
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return aws_detect$quickstream.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"clojure.java.io", (String)"as-url");
    }
}

