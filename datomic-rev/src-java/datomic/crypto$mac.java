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
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class crypto$mac
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.codec", (String)"bytes->string");
    public static final Var const__1 = RT.var((String)"datomic.codec", (String)"encode-64");
    public static final Var const__2 = RT.var((String)"datomic.codec", (String)"string->bytes");

    public static Object invokeStatic(Object plaintext, Object secret) {
        IFn iFn = (IFn)const__0.getRawRoot();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        Mac G__23345 = Mac.getInstance("HmacSHA256");
        Object object = secret;
        secret = null;
        G__23345.init(new SecretKeySpec(((String)object).getBytes(), "HmacSHA256"));
        G__23345.reset();
        Object var2_2 = null;
        Object object2 = plaintext;
        plaintext = null;
        return iFn.invoke(iFn2.invoke((Object)G__23345.doFinal((byte[])((IFn)const__2.getRawRoot()).invoke(object2))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return crypto$mac.invokeStatic(object3, object4);
    }
}

