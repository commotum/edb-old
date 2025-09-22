/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class memory$aws_transactor_settings
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.memory", (String)"transactor-settings");
    public static final Var const__4 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__5 = RT.var((String)"datomic.memory", (String)"aws-instance-mem");

    public static Object invokeStatic(Object instance_type) {
        Object object = instance_type;
        instance_type = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)Numbers.num((long)RT.longCast((double)Numbers.multiply((double)0.7, (Object)((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), object)))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory$aws_transactor_settings.invokeStatic(object2);
    }
}

