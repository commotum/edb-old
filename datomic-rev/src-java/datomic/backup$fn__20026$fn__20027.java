/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.RestFn
 */
package datomic;

import clojure.lang.RestFn;
import java.net.URI;

public final class backup$fn__20026$fn__20027
extends RestFn {
    public Object doInvoke(Object uri2, Object _) {
        Object object = uri2;
        uri2 = null;
        return ((URI)object).getScheme();
    }

    public int getRequiredArity() {
        return 1;
    }
}

