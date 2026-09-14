/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.tools$serialized$fn__21730$fn__21731;

public final class tools$serialized$fn__21730
extends RestFn {
    Object agt;
    Object f;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"send-off");

    public tools$serialized$fn__21730(Object object, Object object2) {
        this.agt = object;
        this.f = object2;
    }

    public Object doInvoke(Object args) {
        Object object = args;
        args = null;
        ((IFn)const__0.getRawRoot()).invoke(this.agt, (Object)new tools$serialized$fn__21730$fn__21731(object, this.f));
        return null;
    }

    public int getRequiredArity() {
        return 0;
    }
}

