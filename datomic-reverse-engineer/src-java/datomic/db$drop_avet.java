/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db.Attribute;

public final class db$drop_avet
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"set-element-fields");
    public static final Keyword const__2 = RT.keyword(null, (String)"index");
    public static final Keyword const__3 = RT.keyword(null, (String)"needsAVET");

    public static Object invokeStatic(Object db2, Object aid, Object _, Object _2) {
        Object object;
        Object unique_QMARK_;
        Object object2 = unique_QMARK_ = ((Attribute)((IFn)db$drop_avet.const__0.getRawRoot()).invoke((Object)db2, (Object)aid)).unique;
        unique_QMARK_ = null;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = db2;
            db2 = null;
            Object object4 = aid;
            aid = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, object4, (Object)const__2, (Object)Boolean.FALSE);
        } else {
            Object object5 = db2;
            db2 = null;
            Object object6 = aid;
            aid = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object5, object6, (Object)const__2, (Object)Boolean.FALSE, (Object)const__3, (Object)Boolean.FALSE);
        }
        return Tuple.create((Object)object);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$drop_avet.invokeStatic(object5, object6, object7, object8);
    }
}

