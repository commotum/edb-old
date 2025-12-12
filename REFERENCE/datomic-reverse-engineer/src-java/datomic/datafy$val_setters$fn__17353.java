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
import datomic.datafy$val_setters$fn__17353$fn__17354;
import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public final class datafy$val_setters$fn__17353
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"keyword");

    public Object invoke(Object m, Object pd) {
        Object object;
        Object object2;
        Method method;
        Method and__5236__auto__17357;
        String name = ((FeatureDescriptor)pd).getName();
        Object object3 = pd;
        pd = null;
        Method method2 = and__5236__auto__17357 = (method = ((PropertyDescriptor)object3).getWriteMethod());
        if (method2 != null && method2 != Boolean.FALSE) {
            object2 = Util.equiv((long)1L, (long)((Object[])method.getParameterTypes()).length) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object2 = and__5236__auto__17357;
            and__5236__auto__17357 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object type = ((IFn)const__3.getRawRoot()).invoke(method.getParameterTypes());
            Object object4 = m;
            m = null;
            String string = name;
            name = null;
            Object object5 = type;
            type = null;
            Method method3 = method;
            method = null;
            datafy$val_setters$fn__17353 this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object4, ((IFn)const__5.getRawRoot()).invoke((Object)string), (Object)new datafy$val_setters$fn__17353$fn__17354(object5, method3));
        } else {
            object = m;
            Object var1_1 = null;
        }
        return object;
    }
}

