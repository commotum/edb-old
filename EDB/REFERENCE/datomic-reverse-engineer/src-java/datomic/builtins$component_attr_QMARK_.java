/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Util;
import datomic.db.Attribute;
import datomic.db.IDbImpl;

public final class builtins$component_attr_QMARK_
extends AFunction {
    public static Object invokeStatic(Object db2, Object a) {
        Object object;
        Object and__5236__auto__23384;
        Object object2 = db2;
        db2 = null;
        Object object3 = a;
        a = null;
        Object attr = ((IDbImpl)object2).elementAt(object3);
        Object object4 = and__5236__auto__23384 = ((Attribute)attr).isComponent;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = attr;
            attr = null;
            object = Util.equiv((long)20L, (Object)((Attribute)object5).vtypeid) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object = and__5236__auto__23384;
            Object var3_3 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return builtins$component_attr_QMARK_.invokeStatic(object3, object4);
    }
}

