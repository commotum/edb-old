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

public final class db$require_kw
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"normalize-kw");
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__2 = RT.keyword((String)"db.error", (String)"nil-keyword");

    public static Object invokeStatic(Object kw) {
        Object object;
        Object or__5238__auto__12515;
        Object object2 = kw;
        kw = null;
        Object object3 = or__5238__auto__12515 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__12515;
            or__5238__auto__12515 = null;
        } else {
            object = ((IFn)const__1.getRawRoot()).invoke((Object)const__2, (Object)"Nil where a keyword was expected");
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$require_kw.invokeStatic(object2);
    }
}

