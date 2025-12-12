/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.connector;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class TransactorHornetConnector$fn__21249$fn__21253$fn__21254$fn__21255
extends AFunction {
    Object tx;
    Object handlers;
    Object session;
    public static final Var const__0 = RT.var((String)"datomic.artemis-client", (String)"create-fressian-message");

    public TransactorHornetConnector$fn__21249$fn__21253$fn__21254$fn__21255(Object object, Object object2, Object object3) {
        this.tx = object;
        this.handlers = object2;
        this.session = object3;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.session, this.handlers, this.tx, (Object)Boolean.FALSE);
        }
        catch (Throwable e2) {
            Object e2 = null;
            object = e2;
        }
        return object;
    }
}

