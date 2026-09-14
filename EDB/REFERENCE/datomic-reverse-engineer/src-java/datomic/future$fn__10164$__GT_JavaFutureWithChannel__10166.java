/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.future.JavaFutureWithChannel;

public final class future$fn__10164$__GT_JavaFutureWithChannel__10166
extends AFunction {
    public Object invoke(Object fut, Object ch) {
        Object object = fut;
        fut = null;
        Object object2 = ch;
        ch = null;
        return new JavaFutureWithChannel(object, object2);
    }
}

