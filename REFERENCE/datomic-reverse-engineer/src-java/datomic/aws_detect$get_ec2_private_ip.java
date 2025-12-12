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

public final class aws_detect$get_ec2_private_ip
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"slurp");
    public static final Var const__1 = RT.var((String)"datomic.aws-detect", (String)"quickstream");
    public static final Object const__2 = 1000L;

    public static Object invokeStatic() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)"http://169.254.169.254/latest/meta-data/local-ipv4", const__2));
        }
        catch (Throwable _) {
            object = null;
        }
        return object;
    }

    public Object invoke() {
        return aws_detect$get_ec2_private_ip.invokeStatic();
    }
}

