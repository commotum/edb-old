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

public final class extension_resolver$load_extensions_config
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.java.io", (String)"resource");
    public static final Var const__1 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"slurp");
    public static final Var const__3 = RT.var((String)"datomic.extension-resolver", (String)"ensure-extensions-config!");
    public static final Var const__4 = RT.var((String)"datomic.extension-resolver", (String)"anomaly!");
    public static final Keyword const__5 = RT.keyword(null, (String)"not-found");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object rsrc) {
        Object object;
        Object temp__5455__auto__14320;
        Object object2 = temp__5455__auto__14320 = ((IFn)const__0.getRawRoot()).invoke(rsrc);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object r;
            Object object3 = temp__5455__auto__14320;
            temp__5455__auto__14320 = null;
            Object object4 = r = object3;
            r = null;
            Object m = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object4));
            ((IFn)const__3.getRawRoot()).invoke(m);
            object = m;
            Object var3_3 = null;
        } else {
            Object object5 = rsrc;
            rsrc = null;
            object = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, ((IFn)const__6.getRawRoot()).invoke((Object)"'", object5, (Object)"' is not on the classpath"));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return extension_resolver$load_extensions_config.invokeStatic(object2);
    }
}

