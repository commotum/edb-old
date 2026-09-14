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

public final class config$read_revision
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.string", (String)"split");
    public static final Object const__3 = Pattern.compile("\\.");
    public static final Object const__4 = 2L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Object const__6 = 999999L;
    public static final Object const__7 = 999999L;

    public static Object invokeStatic(Object props) {
        Object object;
        try {
            Object object2;
            Object object3 = props;
            props = null;
            Object n = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)object3).invoke((Object)"datomic.versionUnique"), const__3, const__4)));
            Object object4 = ((IFn)const__5.getRawRoot()).invoke(n);
            if (object4 != null && object4 != Boolean.FALSE) {
                object2 = n;
                n = null;
            } else {
                object2 = const__6;
            }
            object = object2;
        }
        catch (Throwable t) {
            object = const__7;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$read_revision.invokeStatic(object2);
    }
}

