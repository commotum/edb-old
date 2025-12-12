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
import datomic.pull.AIter;

public final class pull$a_iter
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.pull", (String)"next-a");
    public static final Object const__1 = 0L;

    public static Object invokeStatic(Object db2, Object e) {
        AIter aIter;
        Object temp__5457__auto__18994;
        Object object = temp__5457__auto__18994 = ((IFn)const__0.getRawRoot()).invoke(db2, e, const__1);
        if (object != null && object != Boolean.FALSE) {
            Object object2 = temp__5457__auto__18994;
            temp__5457__auto__18994 = null;
            Object a = object2;
            db2 = null;
            e = null;
            a = null;
            aIter = new AIter(db2, RT.longCast((Object)((Number)e)), RT.longCast((Object)((Number)a)));
        } else {
            aIter = null;
        }
        return aIter;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return pull$a_iter.invokeStatic(object3, object4);
    }
}

