/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class log$log_tree
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.log", (String)"create-log-impl");
    public static final Keyword const__1 = RT.keyword(null, (String)"rev");
    public static final Object const__2 = 0L;
    public static final Keyword const__3 = RT.keyword((String)"d", (String)"l");
    public static final Object const__4 = 3L;
    public static final Keyword const__5 = RT.keyword((String)"d", (String)"r");
    public static final Var const__6 = RT.var((String)"datomic.log", (String)"empty-tail");

    public static Object invokeStatic(Object olookup, Object root_id2) {
        Object object = olookup;
        olookup = null;
        Object[] objectArray = new Object[6];
        objectArray[0] = const__1;
        objectArray[1] = const__2;
        objectArray[2] = const__3;
        objectArray[3] = const__4;
        objectArray[4] = const__5;
        Object object2 = root_id2;
        root_id2 = null;
        objectArray[5] = object2;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)RT.mapUniqueKeys((Object[])objectArray), ((IFn)const__6.getRawRoot()).invoke());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$log_tree.invokeStatic(object3, object4);
    }
}

