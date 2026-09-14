/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.concurrent.BlockingQueue;

public final class queue$queue_seq$drain__12157$fn__12158
extends AFunction {
    Object q;
    Object drain;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"cons");

    public queue$queue_seq$drain__12157$fn__12158(Object object, Object object2) {
        this.q = object;
        this.drain = object2;
    }

    public Object invoke() {
        Object object;
        Object x = ((BlockingQueue)this_.q).take();
        if (x instanceof Throwable) {
            throw (Throwable)x;
        }
        this_.q = null;
        if (Util.identical(x, (Object)this_.q)) {
            object = null;
        } else {
            Object e = x;
            x = null;
            queue$queue_seq$drain__12157$fn__12158 this_ = null;
            object = ((IFn)const__3.getRawRoot()).invoke(e, ((IFn)this_.drain).invoke());
        }
        return object;
    }
}

