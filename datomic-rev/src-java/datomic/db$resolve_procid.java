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

public final class db$resolve_procid
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"qualified-symbol?");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"requiring-resolve!");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__3 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__4 = RT.keyword((String)"db.error", (String)"not-a-data-function");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object db2, Object arg2) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(arg2);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = arg2;
            arg2 = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3);
        } else {
            Object or__5238__auto__13900;
            Object object4 = db2;
            db2 = null;
            Object object5 = or__5238__auto__13900 = ((IFn)const__2.getRawRoot()).invoke(object4, arg2);
            if (object5 != null && object5 != Boolean.FALSE) {
                object = or__5238__auto__13900;
                or__5238__auto__13900 = null;
            } else {
                Object object6 = arg2;
                arg2 = null;
                object = ((IFn)const__3.getRawRoot()).invoke((Object)const__4, ((IFn)const__5.getRawRoot()).invoke((Object)"Unable to resolve data function: ", object6));
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$resolve_procid.invokeStatic(object3, object4);
    }
}

