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
import java.util.regex.Pattern;

public final class slf4j$sanitize_uri
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"replace");
    public static final Object const__1 = Pattern.compile("(aws_access_key_id=)([^&]+)");
    public static final Object const__2 = Pattern.compile("(aws_secret_key=)([^&]+)");

    public static Object invokeStatic(Object uri2) {
        Object object = uri2;
        uri2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(object, const__1, (Object)"$1__SANITIZED__"), const__2, (Object)"$1--SANITIZED--");
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return slf4j$sanitize_uri.invokeStatic(object2);
    }
}

