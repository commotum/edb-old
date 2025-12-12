/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class treewalk$create_node
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)"id");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"lookup");
    public static final Var const__4 = RT.var((String)"datomic.treewalk", (String)"lookup-val");
    public static final Var const__5 = RT.var((String)"datomic.treewalk", (String)"->Node");

    public static Object invokeStatic(Object id, Object lookup, Object allow_missing_QMARK_) {
        Object object;
        Object temp__5457__auto__19731;
        Object object2 = id;
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__1.getRawRoot()).invoke((Object)const__2))));
        }
        Object object3 = lookup;
        if (object3 == null || object3 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__1.getRawRoot()).invoke((Object)const__3))));
        }
        Object object4 = lookup;
        lookup = null;
        Object object5 = allow_missing_QMARK_;
        allow_missing_QMARK_ = null;
        Object object6 = temp__5457__auto__19731 = ((IFn)const__4.getRawRoot()).invoke(object4, id, object5);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = temp__5457__auto__19731;
            temp__5457__auto__19731 = null;
            Object v = object7;
            Object object8 = id;
            id = null;
            Object object9 = v;
            v = null;
            object = ((IFn)const__5.getRawRoot()).invoke(object8, object9);
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return treewalk$create_node.invokeStatic(object4, object5, object6);
    }
}

