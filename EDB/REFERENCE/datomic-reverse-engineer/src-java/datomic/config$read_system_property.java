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

public final class config$read_system_property
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__2 = RT.keyword((String)"db.error", (String)"invalid-config-value");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__4 = RT.keyword(null, (String)"property");
    public static final Keyword const__5 = RT.keyword(null, (String)"value");

    public static Object invokeStatic(Object prop, Object coerce, Object valid_QMARK_, Object object) {
        Object object2;
        String s = System.getProperty((String)prop);
        Object object3 = ((IFn)const__0.getRawRoot()).invoke((Object)s);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = coerce;
            coerce = null;
            Object v = ((IFn)object4).invoke((Object)s);
            Object object5 = valid_QMARK_;
            valid_QMARK_ = null;
            Object object6 = ((IFn)object5).invoke(v);
            if (object6 != null && object6 != Boolean.FALSE) {
                object2 = v;
                v = null;
            } else {
                String string = s;
                s = null;
                Object object7 = ((IFn)const__3.getRawRoot()).invoke((Object)"Invalid value '", (Object)string, (Object)"' for system property '", prop, (Object)"'");
                Object[] objectArray = new Object[4];
                objectArray[0] = const__4;
                Object object8 = prop;
                prop = null;
                objectArray[1] = object8;
                objectArray[2] = const__5;
                Object object9 = v;
                v = null;
                objectArray[3] = object9;
                object2 = ((IFn)const__1.getRawRoot()).invoke((Object)const__2, object7, (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
        } else {
            object2 = object;
            Object var3_3 = null;
        }
        return object2;
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
        return config$read_system_property.invokeStatic(object5, object6, object7, object8);
    }
}

