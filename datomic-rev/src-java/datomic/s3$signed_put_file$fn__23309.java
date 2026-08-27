/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.java.io.Coercions
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.java.io.Coercions;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.io.OutputStream;

public final class s3$signed_put_file$fn__23309
extends AFunction {
    Object os;
    Object f;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    public s3$signed_put_file$fn__23309(Object object, Object object2) {
        this.os = object;
        this.f = object2;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = (IFn)s3$signed_put_file$fn__23309.const__0.getRawRoot();
            v1 = this.f;
            this.f = null;
            v2 = v1;
            if (Util.classOf((Object)v1) == s3$signed_put_file$fn__23309.__cached_class__0) ** GOTO lbl10
            if (!(v2 instanceof Coercions)) {
                v2 = v2;
                s3$signed_put_file$fn__23309.__cached_class__0 = Util.classOf((Object)v2);
lbl10:
                // 2 sources

                v3 = s3$signed_put_file$fn__23309.const__1.getRawRoot().invoke(v2);
            } else {
                v3 = ((Coercions)v2).as_file();
            }
            var1_1 = v0.invoke(v3, this.os);
        }
        finally {
            this.os = null;
            ((OutputStream)this.os).close();
        }
        return var1_1;
    }

    static {
        const__0 = RT.var((String)"clojure.java.io", (String)"copy");
        const__1 = RT.var((String)"clojure.java.io", (String)"as-file");
    }
}

