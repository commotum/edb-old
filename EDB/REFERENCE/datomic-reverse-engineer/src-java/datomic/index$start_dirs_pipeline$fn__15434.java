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

public final class index$start_dirs_pipeline$fn__15434
extends AFunction {
    Object cstore;
    Object last_ex;
    Object serialized_dirs_ch;
    Object olookup;
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"write-dirs");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");

    public index$start_dirs_pipeline$fn__15434(Object object, Object object2, Object object3, Object object4) {
        this.cstore = object;
        this.last_ex = object2;
        this.serialized_dirs_ch = object3;
        this.olookup = object4;
    }

    public Object invoke() {
        Object temp__5457__auto__15436;
        this.cstore = null;
        this.olookup = null;
        this.serialized_dirs_ch = null;
        Object result2 = ((IFn)const__0.getRawRoot()).invoke(this.cstore, this.olookup, this.serialized_dirs_ch);
        this.last_ex = null;
        Object object = temp__5457__auto__15436 = ((IFn)const__1.getRawRoot()).invoke(this.last_ex);
        if (object != null && object != Boolean.FALSE) {
            Object ex;
            Object object2 = temp__5457__auto__15436;
            temp__5457__auto__15436 = null;
            Object object3 = ex = object2;
            ex = null;
            throw (Throwable)object3;
        }
        Object var1_1 = null;
        return result2;
    }
}

