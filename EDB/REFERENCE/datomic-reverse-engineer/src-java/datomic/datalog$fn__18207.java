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

public final class datalog$fn__18207
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__1 = RT.keyword((String)"db.error", (String)"invalid-data-source");
    public static final Keyword const__2 = RT.keyword(null, (String)"input");

    public static Object invokeStatic(Object src, Object consts, Object _, Object _2) {
        Object[] objectArray = new Object[2];
        objectArray[0] = const__2;
        Object object = src;
        src = null;
        objectArray[1] = object;
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, (Object)"Nil or missing data source. Did you forget to pass a database argument?", (Object)RT.mapUniqueKeys((Object[])objectArray));
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
        return datalog$fn__18207.invokeStatic(object5, object6, object7, object8);
    }
}

