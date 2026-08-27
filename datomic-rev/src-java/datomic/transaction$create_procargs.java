/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class transaction$create_procargs
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"id");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"rand-uuid");
    public static final Keyword const__2 = RT.keyword(null, (String)"data");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__4 = RT.keyword(null, (String)"options");

    public static Object invokeStatic(Object tx, Object options) {
        Object object;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__0;
        objectArray[1] = ((IFn)const__1.getRawRoot()).invoke();
        objectArray[2] = const__2;
        Object object2 = tx;
        tx = null;
        objectArray[3] = object2;
        IPersistentMap G__15936 = RT.mapUniqueKeys((Object[])objectArray);
        Object object3 = options;
        if (object3 != null && object3 != Boolean.FALSE) {
            IPersistentMap iPersistentMap = G__15936;
            G__15936 = null;
            Object object4 = options;
            options = null;
            object = ((IFn)const__3.getRawRoot()).invoke((Object)iPersistentMap, (Object)const__4, object4);
        } else {
            object = G__15936;
            Object var2_2 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return transaction$create_procargs.invokeStatic(object3, object4);
    }
}

