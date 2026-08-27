/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.Arrays;

public final class catalog$deleted_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__3 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"map?"), Symbol.intern(null, (String)"catalog")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__6 = RT.var((String)"datomic.catalog", (String)"db-ids");
    public static final Keyword const__8 = RT.keyword((String)"datomic", (String)"deleted");

    public static Object invokeStatic(Object catalog2, Object db_id) {
        Object object;
        Object and__5236__auto__11154;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(catalog2);
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke(const__3))));
        }
        Object object3 = and__5236__auto__11154 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(catalog2), db_id));
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = catalog2;
            catalog2 = null;
            Object object5 = db_id;
            db_id = null;
            object = ((IFn)const__5.getRawRoot()).invoke(RT.get((Object)object4, (Object)const__8), object5);
        } else {
            object = and__5236__auto__11154;
            Object var2_2 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return catalog$deleted_QMARK_.invokeStatic(object3, object4);
    }
}

