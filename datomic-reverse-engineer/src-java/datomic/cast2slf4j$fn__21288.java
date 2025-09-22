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

public final class cast2slf4j$fn__21288
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"clojure.string", (String)"capitalize");
    public static final Var const__5 = RT.var((String)"clojure.string", (String)"split");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"name");
    public static final Object const__7 = Pattern.compile("\\.");

    public static Object invokeStatic(Object mname) {
        Object object = mname;
        mname = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(object), const__7))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cast2slf4j$fn__21288.invokeStatic(object2);
    }
}

