/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.Attribute;
import datomic.db.IDbImpl;

public final class excise$component_attr_QMARK_
extends AFunction {
    public static Object invokeStatic(Object db2, Object a) {
        Object object = db2;
        db2 = null;
        Object object2 = a;
        a = null;
        return ((Attribute)((IDbImpl)object).elementAt((Object)object2)).isComponent;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return excise$component_attr_QMARK_.invokeStatic(object3, object4);
    }
}

