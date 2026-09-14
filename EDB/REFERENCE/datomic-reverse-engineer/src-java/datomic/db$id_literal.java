/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.DbId;

public final class db$id_literal
extends AFunction {
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"next-id");

    public static Object invokeStatic(Object literal) {
        Object object;
        Object or__5238__auto__12441;
        Object object2 = RT.nth((Object)literal, (int)RT.uncheckedIntCast((long)0L));
        Object object3 = literal;
        literal = null;
        Object object4 = or__5238__auto__12441 = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)1L), null);
        if (object4 != null && object4 != Boolean.FALSE) {
            object = or__5238__auto__12441;
            or__5238__auto__12441 = null;
        } else {
            object = Numbers.unchecked_minus((Object)((IFn)const__4.getRawRoot()).invoke());
        }
        return new DbId(object2, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$id_literal.invokeStatic(object2);
    }
}

