/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class tools$pace_fn$fn__21787
extends AFunction {
    Object last_val_gets;
    Object pace_msec;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.kv-cluster", (String)"val-gets-ref");
    public static final Var const__6 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__7 = RT.keyword(null, (String)"ToolsPaceReadMsec");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"reset!");

    public tools$pace_fn$fn__21787(Object object, Object object2) {
        this.last_val_gets = object;
        this.pace_msec = object2;
    }

    public Object invoke() {
        Object object;
        Object new_val_gets = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
        Number msec = Numbers.multiply((Object)Numbers.minus((Object)new_val_gets, (Object)((IFn)const__0.getRawRoot()).invoke(this_.last_val_gets)), (Object)this_.pace_msec);
        if (Numbers.lt((long)10L, (Object)msec)) {
            ((IFn)const__6.getRawRoot()).invoke((Object)const__7, (Object)msec);
            Number number = msec;
            msec = null;
            Thread.sleep(RT.longCast((Object)number));
            Object object2 = new_val_gets;
            new_val_gets = null;
            tools$pace_fn$fn__21787 this_ = null;
            object = ((IFn)const__8.getRawRoot()).invoke(this_.last_val_gets, object2);
        } else {
            object = null;
        }
        return object;
    }
}

