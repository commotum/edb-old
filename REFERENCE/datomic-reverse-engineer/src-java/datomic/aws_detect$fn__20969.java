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

public final class aws_detect$fn__20969
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.aws-detect", (String)"get-ec2-public-ip");

    public static Object invokeStatic() {
        return RT.booleanCast((Object)((IFn)const__1.getRawRoot()).invoke()) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke() {
        return aws_detect$fn__20969.invokeStatic();
    }
}

