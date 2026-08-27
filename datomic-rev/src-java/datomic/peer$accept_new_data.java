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
import datomic.Database;
import datomic.db.IDbImpl;
import datomic.peer$accept_new_data$fn__21482;

public final class peer$accept_new_data
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"drop-while");

    public static Object invokeStatic(Object db2, Object data2) {
        Object object;
        Object temp__5455__auto__21485;
        long nextT = ((Database)db2).nextT();
        Object object2 = data2;
        data2 = null;
        Object object3 = temp__5455__auto__21485 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)new peer$accept_new_data$fn__21482(nextT), object2));
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5455__auto__21485;
            temp__5455__auto__21485 = null;
            Object newdata = object4;
            Object object5 = db2;
            db2 = null;
            Object object6 = newdata;
            newdata = null;
            object = ((IDbImpl)object5).acceptDataCheck(object6, Boolean.FALSE);
        } else {
            object = db2;
            Object object7 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return peer$accept_new_data.invokeStatic(object3, object4);
    }
}

