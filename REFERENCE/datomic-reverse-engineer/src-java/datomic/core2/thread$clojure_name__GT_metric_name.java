/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.regex.Pattern;

public final class thread$clojure_name__GT_metric_name
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"join");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.string", (String)"capitalize");
    public static final Var const__3 = RT.var((String)"clojure.string", (String)"split");
    public static final Object const__4 = Pattern.compile("\\W");

    public static Object invokeStatic(Object s) {
        Object object = s;
        s = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)".", ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(object, const__4)));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return thread$clojure_name__GT_metric_name.invokeStatic(object2);
    }
}

