/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.excise$create_es_pred$reify__14859;

public final class excise$create_es_pred
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.excise", (String)"create-e->xpreds");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"cat");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"vals");
    public static final AFn const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 165, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object db2, Object specs) {
        Object object = specs;
        specs = null;
        Object e__GT_xpreds = ((IFn)const__0.getRawRoot()).invoke(db2, object);
        Object xpreds = ((IFn)const__1.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(e__GT_xpreds));
        Object object2 = e__GT_xpreds;
        e__GT_xpreds = null;
        Object object3 = db2;
        db2 = null;
        Object object4 = xpreds;
        xpreds = null;
        return ((IObj)new excise$create_es_pred$reify__14859(null, object2, object3, object4)).withMeta((IPersistentMap)const__8);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return excise$create_es_pred.invokeStatic(object3, object4);
    }
}

