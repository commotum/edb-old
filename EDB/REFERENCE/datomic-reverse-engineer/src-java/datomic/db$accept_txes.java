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

public final class db$accept_txes
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"add-fulltext");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"transduce");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__3 = RT.keyword(null, (String)"data");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"completing");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"accept-data-no-check");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"mapcat");

    public static Object invokeStatic(Object db2, Object txes) {
        Object object = db2;
        db2 = null;
        Object object2 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot()), object, txes);
        Object object3 = txes;
        txes = null;
        return ((IFn)const__0.getRawRoot()).invoke(object2, ((IFn)const__6.getRawRoot()).invoke((Object)const__3, object3));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$accept_txes.invokeStatic(object3, object4);
    }
}

