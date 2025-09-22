/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$require_attrid
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"require-id");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"not-an-attribute");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object db2, Object x) {
        Object object;
        long id = ((IFn.OOL)const__0.getRawRoot()).invokePrim(db2, x);
        Object object2 = db2;
        db2 = null;
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(object2, (Object)Numbers.num((long)id));
        if (object3 != null && object3 != Boolean.FALSE) {
            object = Numbers.num((long)id);
        } else {
            Object object4 = x;
            x = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)const__3, ((IFn)const__4.getRawRoot()).invoke(object4, (Object)" is not an attribute."));
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$require_attrid.invokeStatic(object3, object4);
    }
}

