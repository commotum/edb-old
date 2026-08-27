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

public final class db$wrong_type_for_attribute
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__1 = RT.keyword((String)"db.error", (String)"wrong-type-for-attribute");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"resolve-kw");

    public static Object invokeStatic(Object db2, Object attrid, Object tag, Object v) {
        Object object = v;
        v = null;
        Object object2 = tag;
        tag = null;
        Object object3 = ((IFn)const__3.getRawRoot()).invoke(db2, object2);
        Object object4 = db2;
        db2 = null;
        Object object5 = attrid;
        attrid = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, ((IFn)const__2.getRawRoot()).invoke((Object)"Value ", object, (Object)" is not a valid ", object3, (Object)" for attribute ", ((IFn)const__3.getRawRoot()).invoke(object4, object5)));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$wrong_type_for_attribute.invokeStatic(object5, object6, object7, object8);
    }
}

