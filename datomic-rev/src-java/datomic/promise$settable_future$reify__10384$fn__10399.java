/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPending
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPending;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class promise$settable_future$reify__10384$fn__10399
extends AFunction {
    Object this;
    Object exec;
    Object listener;
    Object lockee__5436__auto__;
    Object listeners;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"conj");

    public promise$settable_future$reify__10384$fn__10399(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.this = object;
        this.exec = object2;
        this.listener = object3;
        this.lockee__5436__auto__ = object4;
        this.listeners = object5;
    }

    public Object invoke() {
        Boolean bl;
        try {
            synchronized (this.lockee__5436__auto__) {
                Boolean bl2;
                if (((IPending)this.this).isRealized()) {
                    bl2 = Boolean.TRUE;
                } else {
                    this.listener = null;
                    this.exec = null;
                    ((IFn)const__0.getRawRoot()).invoke(this.listeners, const__1.getRawRoot(), (Object)Tuple.create((Object)this.listener, (Object)this.exec));
                    bl2 = Boolean.FALSE;
                }
                bl = bl2;
            }
        }
        finally {
            this.lockee__5436__auto__ = null;
            // ** MonitorExit[this.lockee__5436__auto__] (shouldn't be in output)
        }
        {
            return bl;
        }
    }
}

