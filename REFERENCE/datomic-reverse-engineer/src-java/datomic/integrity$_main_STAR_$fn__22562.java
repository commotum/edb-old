/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.integrity$_main_STAR_$fn__22562$fn__22563;

public final class integrity$_main_STAR_$fn__22562
extends AFunction {
    Object uri;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__1 = RT.var((String)"datomic.integrity", (String)"pod-storage-seq");

    public integrity$_main_STAR_$fn__22562(Object object) {
        this.uri = object;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke((Object)new integrity$_main_STAR_$fn__22562$fn__22563(), ((IFn)const__1.getRawRoot()).invoke(this.uri));
        }
        catch (Throwable t__22555__auto__2) {
            Object t__22555__auto__2 = null;
            t__22555__auto__2.printStackTrace();
            object = null;
        }
        return object;
    }
}

