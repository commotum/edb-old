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

public final class integrity$_main_STAR_$fn__22560
extends AFunction {
    Object uri;
    public static final Var const__0 = RT.var((String)"datomic.integrity", (String)"diagnostics");

    public integrity$_main_STAR_$fn__22560(Object object) {
        this.uri = object;
    }

    public Object invoke() {
        Object object;
        try {
            this.uri = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.uri);
        }
        catch (Throwable t__22555__auto__2) {
            Object t__22555__auto__2 = null;
            t__22555__auto__2.printStackTrace();
            object = null;
        }
        return object;
    }
}

