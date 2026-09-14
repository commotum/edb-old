/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class btset$bench$fn__11881
extends AFunction {
    Object ss;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"subseq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"<");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"first");

    public btset$bench$fn__11881(Object object) {
        this.ss = object;
    }

    public Object invoke() {
        Object s = ((IFn)const__0.getRawRoot()).invoke(this.ss);
        Object ret = Numbers.num((long)0L);
        while (true) {
            Object object = s;
            if (object == null || object == Boolean.FALSE) break;
            Object object2 = ((IFn)const__3.getRawRoot()).invoke(s);
            Object object3 = s;
            s = null;
            ret = ((IFn)const__4.getRawRoot()).invoke(this.ss, const__5.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke(object3));
            s = object2;
        }
        Object var2_2 = null;
        return ret;
    }
}

