/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index_checks$_main$fn__21957
extends AFunction {
    Object uri;
    public static final Var const__0 = RT.var((String)"datomic.tools.index-checks", (String)"-main*");
    public static final Var const__1 = RT.var((String)"datomic.api", (String)"shutdown");

    public index_checks$_main$fn__21957(Object object) {
        this.uri = object;
    }

    public Object invoke() {
        Object object;
        try {
            this.uri = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.uri);
        }
        catch (Throwable t2) {
            ((IFn)const__1.getRawRoot()).invoke((Object)Boolean.TRUE);
            Object t2 = null;
            t2.printStackTrace();
            System.exit(RT.intCast((long)-1L));
            object = null;
        }
        return object;
    }
}

