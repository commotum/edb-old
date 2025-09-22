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

public final class db$normalize_kw
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"to-kw");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keyword?");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"not-a-keyword");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"class");

    public static Object invokeStatic(Object kw) {
        Object object;
        Object temp__5457__auto__12513;
        Object object2 = temp__5457__auto__12513 = ((IFn)const__0.getRawRoot()).invoke(kw);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5457__auto__12513;
            temp__5457__auto__12513 = null;
            Object result2 = object3;
            Object object4 = ((IFn)const__1.getRawRoot()).invoke(result2);
            if (object4 != null && object4 != Boolean.FALSE) {
            } else {
                Object object5 = kw;
                Object object6 = kw;
                kw = null;
                ((IFn)const__2.getRawRoot()).invoke((Object)const__3, ((IFn)const__4.getRawRoot()).invoke((Object)"Cannot interpret as a keyword: ", object5, (Object)" of class: ", ((IFn)const__5.getRawRoot()).invoke(object6)));
            }
            object = result2;
            Object var2_2 = null;
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$normalize_kw.invokeStatic(object2);
    }
}

