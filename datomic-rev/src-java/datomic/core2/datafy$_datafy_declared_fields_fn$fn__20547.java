/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic.core2;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class datafy$_datafy_declared_fields_fn$fn__20547
extends AFunction {
    public Object invoke(Object f) {
        Object object = f;
        f = null;
        datafy$_datafy_declared_fields_fn$fn__20547 this_ = null;
        return Numbers.isZero((long)((long)Modifier.STATIC & (long)((Field)object).getModifiers())) ? Boolean.TRUE : Boolean.FALSE;
    }
}

