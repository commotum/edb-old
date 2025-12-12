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
import datomic.datalog.ExtRel;

public final class datalog$fn__18213
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object src, Object consts, Object _, Object _2) {
        Object object;
        Object object2 = src;
        src = null;
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(object2);
        if (Util.classOf((Object)object3) != __cached_class__0) {
            if (object3 instanceof ExtRel) {
                Object object4 = consts;
                consts = null;
                object = ((ExtRel)object3).extrel(object4, null, null);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object5 = consts;
        consts = null;
        object = const__0.getRawRoot().invoke(object3, object5, null, null);
        return object;
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
        return datalog$fn__18213.invokeStatic(object5, object6, object7, object8);
    }

    static {
        const__0 = RT.var((String)"datomic.datalog", (String)"extrel");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
    }
}

