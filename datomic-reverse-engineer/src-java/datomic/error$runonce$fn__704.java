/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Util;
import clojure.lang.Var;

public final class error$runonce$fn__704
extends RestFn {
    Object f;
    Object result;
    Object sentinel;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"apply");

    public error$runonce$fn__704(Object object, Object object2, Object object3) {
        this.f = object;
        this.result = object2;
        this.sentinel = object3;
    }

    public Object doInvoke(Object args) {
        Object object;
        Object lockee__5436__auto__706 = this.sentinel;
        try {
            synchronized (lockee__5436__auto__706) {
                Object object2;
                if (Util.equiv((Object)((IFn)const__1.getRawRoot()).invoke(this.result), (Object)this.sentinel)) {
                    Object object3 = args;
                    args = null;
                    object2 = ((IFn)const__2.getRawRoot()).invoke(this.result, ((IFn)const__3.getRawRoot()).invoke(this.f, object3));
                } else {
                    object2 = ((IFn)const__1.getRawRoot()).invoke(this.result);
                }
                object = object2;
            }
        }
        finally {
            Object object4 = lockee__5436__auto__706;
            lockee__5436__auto__706 = null;
            // ** MonitorExit[v2] (shouldn't be in output)
        }
        {
            return object;
        }
    }

    public int getRequiredArity() {
        return 0;
    }
}

