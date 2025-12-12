/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.IDbImpl;

public final class db$fulltext_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");

    public static Object invokeStatic(Object db2, Object attr) {
        Object object;
        Object temp__5457__auto__13255;
        Object object2 = attr;
        attr = null;
        Object object3 = temp__5457__auto__13255 = ((IFn)const__0.getRawRoot()).invoke(db2, object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object temp__5457__auto__13254;
            Object object4 = temp__5457__auto__13255;
            temp__5457__auto__13255 = null;
            Object attrid = object4;
            Object object5 = db2;
            db2 = null;
            Object object6 = attrid;
            attrid = null;
            Object object7 = temp__5457__auto__13254 = ((IDbImpl)object5).elementAt(object6);
            if (object7 != null && object7 != Boolean.FALSE) {
                Object a;
                Object object8 = temp__5457__auto__13254;
                temp__5457__auto__13254 = null;
                Object object9 = a = object8;
                a = null;
                object = ((Attribute)object9).fulltext;
            } else {
                object = null;
            }
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
        return db$fulltext_QMARK_.invokeStatic(object3, object4);
    }
}

