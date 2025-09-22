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
package datomic.reconnector2;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.reconnector2.Reconnector$fn__17129$fn__17130;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class Reconnector$fn__17129
extends AFunction {
    Object current_promise_ref;
    Object shutdown_state;
    Object worker_ref;
    Object cleanup_fn;
    Object this;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"future-cancel");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"future-call");
    public static final Var const__5 = RT.var((String)"datomic.promise", (String)"delivered");

    public Reconnector$fn__17129(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.current_promise_ref = object;
        this.shutdown_state = object2;
        this.worker_ref = object3;
        this.cleanup_fn = object4;
        this.this = object5;
    }

    public Object invoke() {
        Object object;
        Object lockee__5436__auto__17133 = this.worker_ref;
        try {
            synchronized (lockee__5436__auto__17133) {
                Object object2;
                Object object3 = ((IFn)const__0.getRawRoot()).invoke(this.worker_ref);
                if (object3 != null && object3 != Boolean.FALSE) {
                    ((IFn)const__1.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(this.worker_ref));
                    ((IFn)((IFn)const__0.getRawRoot()).invoke(this.current_promise_ref)).invoke(this.shutdown_state);
                    ((IFn)const__2.getRawRoot()).invoke(this.worker_ref, null);
                }
                Object state2 = ((IFn)const__0.getRawRoot()).invoke(this.this);
                if (Util.equiv((Object)state2, (Object)this.shutdown_state)) {
                    object2 = null;
                } else {
                    Object object4 = state2;
                    state2 = null;
                    ((IFn)const__4.getRawRoot()).invoke((Object)new Reconnector$fn__17129$fn__17130(object4, this.cleanup_fn));
                    object2 = ((IFn)const__2.getRawRoot()).invoke(this.current_promise_ref, ((IFn)const__5.getRawRoot()).invoke(this.shutdown_state));
                }
                object = object2;
            }
        }
        finally {
            Object object5 = lockee__5436__auto__17133;
            lockee__5436__auto__17133 = null;
            // ** MonitorExit[v3] (shouldn't be in output)
        }
        {
            return object;
        }
    }
}

