/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  clojure.lang.Volatile
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import clojure.lang.Volatile;

public final class common$distinct_by$fn__9243$fn__9244
extends AFunction {
    Object seen;
    Object f;
    Object rf;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"conj");

    public common$distinct_by$fn__9243$fn__9244(Object object, Object object2, Object object3) {
        this.seen = object;
        this.f = object2;
        this.rf = object3;
    }

    public Object invoke(Object result2, Object input) {
        Object object;
        Object k = ((IFn)this_.f).invoke(input);
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this_.seen), k);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = result2;
            result2 = null;
        } else {
            Object object3 = k;
            k = null;
            ((Volatile)this_.seen).reset(((IFn)const__2.getRawRoot()).invoke(((Volatile)this_.seen).deref(), object3));
            Object object4 = result2;
            result2 = null;
            Object object5 = input;
            input = null;
            common$distinct_by$fn__9243$fn__9244 this_ = null;
            object = ((IFn)this_.rf).invoke(object4, object5);
        }
        return object;
    }

    public Object invoke(Object result2) {
        Object object = result2;
        result2 = null;
        common$distinct_by$fn__9243$fn__9244 this_ = null;
        return ((IFn)this_.rf).invoke(object);
    }

    public Object invoke() {
        common$distinct_by$fn__9243$fn__9244 this_ = null;
        return ((IFn)this_.rf).invoke();
    }
}

