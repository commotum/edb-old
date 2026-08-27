/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class aws_monitor$cloudwatch_reporter
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.aws-monitor", (String)"cloudwatch-reporter-ref");

    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return ((IFn)((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot())).invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aws_monitor$cloudwatch_reporter.invokeStatic(object2);
    }
}

