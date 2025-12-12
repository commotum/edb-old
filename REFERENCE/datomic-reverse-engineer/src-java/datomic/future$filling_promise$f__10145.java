/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class future$filling_promise$f__10145
extends AFunction {
    Object f;
    Object ch;
    public static final Var const__1 = RT.var((String)"clojure.core.async", (String)">!!");
    public static final Keyword const__2 = RT.keyword((String)"datomic.future", (String)"nil");

    public future$filling_promise$f__10145(Object object, Object object2) {
        this.f = object;
        this.ch = object2;
    }

    public Object invoke() {
        Object object;
        try {
            Object v = ((IFn)this.f).invoke();
            if (Util.identical((Object)v, null)) {
                ((IFn)const__1.getRawRoot()).invoke(this.ch, (Object)const__2);
            } else {
                ((IFn)const__1.getRawRoot()).invoke(this.ch, v);
            }
            Object object2 = v;
            v = null;
            object = object2;
        }
        catch (Throwable t2) {
            ((IFn)const__1.getRawRoot()).invoke(this.ch, (Object)t2);
            Object t2 = null;
            throw t2;
        }
        return object;
    }
}

