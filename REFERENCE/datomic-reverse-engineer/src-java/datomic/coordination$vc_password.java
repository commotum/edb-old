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
import java.security.MessageDigest;

public final class coordination$vc_password
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.codec", (String)"bytes->string");
    public static final Var const__1 = RT.var((String)"datomic.codec", (String)"encode-64");

    public static Object invokeStatic(Object peer_password) {
        Object object = peer_password;
        peer_password = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)MessageDigest.getInstance("MD5").digest(((String)object).getBytes())));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return coordination$vc_password.invokeStatic(object2);
    }
}

