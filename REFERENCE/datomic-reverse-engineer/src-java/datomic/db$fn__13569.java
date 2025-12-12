/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class db$fn__13569
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"pr");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__10 = (AFn)RT.vector((Object[])new Object[]{RT.keyword(null, (String)"id"), RT.keyword(null, (String)"basisT"), RT.keyword(null, (String)"indexBasisT"), RT.keyword(null, (String)"index-root-id"), RT.keyword(null, (String)"asOfT"), RT.keyword(null, (String)"sinceT"), RT.keyword(null, (String)"raw")});
    public static final Keyword const__11 = RT.keyword(null, (String)"type");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"datomic.db.Db");

    public static Object invokeStatic(Object db2) {
        Object object = db2;
        db2 = null;
        return ((IFn)const__0.get()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object, (Object)const__10), (Object)const__11, (Object)const__12));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$fn__13569.invokeStatic(object2);
    }
}

