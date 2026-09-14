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

public final class config$property
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.config", (String)"initialized");
    public static final Var const__2 = RT.var((String)"datomic.config", (String)"properties-ref");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__5 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__6 = RT.keyword((String)"db.error", (String)"not-a-config-property");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__8 = RT.keyword(null, (String)"property");

    public static Object invokeStatic(Object name) {
        Object object;
        ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
        Object props = ((IFn)const__0.getRawRoot()).invoke(const__2.getRawRoot());
        Object object2 = ((IFn)const__3.getRawRoot()).invoke(props, name);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = props;
            props = null;
            Object object4 = name;
            name = null;
            object = RT.get((Object)object3, (Object)object4);
        } else {
            Object object5 = ((IFn)const__7.getRawRoot()).invoke((Object)"No config property named '", name, (Object)"'");
            Object[] objectArray = new Object[2];
            objectArray[0] = const__8;
            Object object6 = name;
            name = null;
            objectArray[1] = object6;
            object = ((IFn)const__5.getRawRoot()).invoke((Object)const__6, object5, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$property.invokeStatic(object2);
    }
}

