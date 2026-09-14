/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class s3$s3_service
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"datomic.s3-api", (String)"client");

    public static Object invokeStatic(ISeq args) {
        ISeq iSeq = args;
        args = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), (Object)iSeq);
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return s3$s3_service.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

