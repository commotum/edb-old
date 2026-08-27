/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class callback$compile_static_method_callback$fn__10095
extends AFunction {
    Object cname;

    public callback$compile_static_method_callback$fn__10095(Object object) {
        this.cname = object;
    }

    public Object invoke() {
        Class<?> clazz;
        try {
            clazz = Class.forName((String)this.cname);
        }
        catch (Throwable _) {
            clazz = null;
        }
        return clazz;
    }
}

