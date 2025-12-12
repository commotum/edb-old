/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;
import datomic.db.Attribute;

public final class db$tuple_attr_value_type
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"tuple-value-types");

    public static Object invokeStatic(Object db2, Object attr_id) {
        Object object;
        Object object2;
        Object and__5236__auto__13058;
        Object object3;
        Object G__13056;
        Object object4;
        Object attr;
        Object object5;
        Object object6 = attr_id;
        attr_id = null;
        Object G__13055 = ((IFn)const__0.getRawRoot()).invoke(db2, object6);
        if (Util.identical((Object)G__13055, null)) {
            object5 = null;
        } else {
            Object object7 = G__13055;
            G__13055 = null;
            object5 = ((IFn)const__2.getRawRoot()).invoke(db2, object7);
        }
        Object G__130562 = attr = object5;
        if (Util.identical(G__130562, null)) {
            object4 = null;
        } else {
            G__130562 = null;
            object4 = G__13056 = ((Attribute)G__130562).vtypeid;
        }
        if (Util.identical((Object)G__13056, null)) {
            object3 = null;
        } else {
            Object object8 = db2;
            db2 = null;
            Object object9 = G__13056;
            G__13056 = null;
            object3 = ((Database)object8).ident(object9);
        }
        Object type = object3;
        Object object10 = and__5236__auto__13058 = ((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), type);
        if (object10 != null && object10 != Boolean.FALSE) {
            Object object11 = attr;
            attr = null;
            object2 = Util.equiv((long)35L, (Object)((Attribute)object11).cardinality) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object2 = and__5236__auto__13058;
            and__5236__auto__13058 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = type;
            type = null;
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$tuple_attr_value_type.invokeStatic(object3, object4);
    }
}

