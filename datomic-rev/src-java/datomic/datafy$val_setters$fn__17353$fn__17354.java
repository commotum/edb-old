/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.lang.reflect.Method;

public final class datafy$val_setters$fn__17353$fn__17354
extends AFunction {
    Object type;
    Object method;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Var const__1 = RT.var((String)"datomic.datafy", (String)"data-to-object");

    public datafy$val_setters$fn__17353$fn__17354(Object object, Object object2) {
        this.type = object;
        this.method = object2;
    }

    public Object invoke(Object bean, Object value) {
        Object object = bean;
        bean = null;
        Object object2 = value;
        value = null;
        return ((Method)this.method).invoke(object, (Object[])((IFn)const__0.getRawRoot()).invoke((Object)Tuple.create((Object)((IFn)const__1.getRawRoot()).invoke(object2, this.type))));
    }
}

