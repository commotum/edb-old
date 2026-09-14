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

public final class common$bean_setters$fn__9186$fn__9187
extends AFunction {
    Object method;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into-array");

    public common$bean_setters$fn__9186$fn__9187(Object object) {
        this.method = object;
    }

    public Object invoke(Object bean, Object value) {
        Object object = bean;
        bean = null;
        Object object2 = value;
        value = null;
        return ((Method)this.method).invoke(object, (Object[])((IFn)const__0.getRawRoot()).invoke((Object)Tuple.create((Object)object2)));
    }
}

