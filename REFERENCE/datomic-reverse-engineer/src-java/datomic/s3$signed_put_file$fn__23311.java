/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.io.InputStream;

public final class s3$signed_put_file$fn__23311
extends AFunction {
    Object is;

    public s3$signed_put_file$fn__23311(Object object) {
        this.is = object;
    }

    public Object invoke() {
        Object var1_1;
        try {
            var1_1 = null;
        }
        finally {
            this.is = null;
            ((InputStream)this.is).close();
        }
        return var1_1;
    }
}

