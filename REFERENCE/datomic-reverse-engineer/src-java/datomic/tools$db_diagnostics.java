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
import datomic.tools$db_diagnostics$fn__21800;
import datomic.tools$db_diagnostics$fn__21802;

public final class tools$db_diagnostics
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.stats", (String)"sizes");
    public static final Keyword const__1 = RT.keyword(null, (String)"with-key-summary");
    public static final Keyword const__2 = RT.keyword(null, (String)"index-sizes");
    public static final Keyword const__3 = RT.keyword(null, (String)"index-metrics");
    public static final Var const__4 = RT.var((String)"datomic.stats", (String)"sizes->metrics");

    public static Object invokeStatic(Object uri2) {
        IPersistentMap iPersistentMap;
        Object sizes2;
        Object object;
        Object db2;
        Object object2;
        Object conn;
        Object object3 = uri2;
        uri2 = null;
        Object object4 = conn = ((IFn)new tools$db_diagnostics$fn__21800(object3)).invoke();
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = conn;
            conn = null;
            object2 = ((IFn)new tools$db_diagnostics$fn__21802(object5)).invoke();
        } else {
            object2 = null;
        }
        Object object6 = db2 = object2;
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = db2;
            db2 = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object7, (Object)const__1, (Object)Boolean.TRUE);
        } else {
            object = null;
        }
        Object object8 = sizes2 = object;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object[] objectArray = new Object[4];
            objectArray[0] = const__2;
            objectArray[1] = sizes2;
            objectArray[2] = const__3;
            Object object9 = sizes2;
            sizes2 = null;
            objectArray[3] = ((IFn)const__4.getRawRoot()).invoke(object9);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        return iPersistentMap;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$db_diagnostics.invokeStatic(object2);
    }
}

