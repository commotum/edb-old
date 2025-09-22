/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.io.FileNotFoundException;

public final class config$fn__867$fn__868
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"require");
    public static final AFn const__1 = (AFn)Symbol.intern(null, (String)"datomic.memcached.folsom");

    public Object invoke() {
        Boolean bl;
        try {
            ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
            bl = Boolean.TRUE;
        }
        catch (FileNotFoundException _) {
            bl = Boolean.FALSE;
        }
        return bl;
    }
}

