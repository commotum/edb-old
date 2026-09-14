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

public final class datalog$fn__18209
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.datalog", (String)"extrel-coll");
    public static final Var const__3 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__4 = RT.keyword((String)"db.error", (String)"invalid-data-source");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__6 = RT.keyword(null, (String)"input");

    public static Object invokeStatic(Object src, Object consts, Object _, Object _2) {
        Object object;
        if (src instanceof Iterable) {
            Object object2 = src;
            src = null;
            Object object3 = consts;
            consts = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object2, object3);
        } else {
            Object object4 = ((IFn)const__5.getRawRoot()).invoke(src.getClass(), (Object)" is not a valid data source type.");
            Object[] objectArray = new Object[2];
            objectArray[0] = const__6;
            Object object5 = src;
            src = null;
            objectArray[1] = object5;
            object = ((IFn)const__3.getRawRoot()).invoke((Object)const__4, object4, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
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
        return datalog$fn__18209.invokeStatic(object5, object6, object7, object8);
    }
}

