/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic.core2.aws.s3;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class sdkv1$wrap_ex_handler$fn__20533
extends RestFn {
    Object f;
    Object context;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__2 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"throwable->anom");

    public sdkv1$wrap_ex_handler$fn__20533(Object object, Object object2) {
        this.f = object;
        this.context = object2;
    }

    public Object doInvoke(Object args) {
        Object object;
        try {
            Object object2 = args;
            args = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.f, object2);
        }
        catch (Throwable t2) {
            Object t2 = null;
            object = ((IFn)const__1.getRawRoot()).invoke(this.context, ((IFn)const__2.getRawRoot()).invoke((Object)t2));
        }
        return object;
    }

    public int getRequiredArity() {
        return 0;
    }
}

