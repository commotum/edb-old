/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.excise$create_as_pred$reify__14869;

public final class excise$create_as_pred
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.excise", (String)"create-a->xpreds");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 196, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object db2, Object specs) {
        Object a__GT_xpreds;
        Object object = db2;
        db2 = null;
        Object object2 = specs;
        specs = null;
        Object object3 = a__GT_xpreds = ((IFn)const__0.getRawRoot()).invoke(object, object2);
        a__GT_xpreds = null;
        return ((IObj)new excise$create_as_pred$reify__14869(null, object3)).withMeta((IPersistentMap)const__5);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return excise$create_as_pred.invokeStatic(object3, object4);
    }
}

