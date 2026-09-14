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

public final class fressian$fressianable_QMARK_$fn__12221
extends AFunction {
    Object handlers;
    Object val;
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"fressian-val");

    public fressian$fressianable_QMARK_$fn__12221(Object object, Object object2) {
        this.handlers = object;
        this.val = object2;
    }

    public Object invoke() {
        Object object;
        try {
            this.val = null;
            this.handlers = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.val, this.handlers);
        }
        catch (Throwable _) {
            object = Boolean.FALSE;
        }
        return object;
    }
}

