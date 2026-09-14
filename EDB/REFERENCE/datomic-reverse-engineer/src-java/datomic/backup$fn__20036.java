/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import java.net.URI;

public final class backup$fn__20036
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__1 = RT.keyword((String)"storage", (String)"invalid-uri");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object uri2, Object sse_QMARK_) {
        Object object = uri2;
        uri2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, ((IFn)const__2.getRawRoot()).invoke((Object)"Unsupported protocol: ", (Object)((URI)object).getScheme()));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$fn__20036.invokeStatic(object3, object4);
    }
}

