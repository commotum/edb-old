/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.backup;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class Substorage$fn__20013
extends AFunction {
    Object prefix;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__2 = RT.var((String)"datomic.backup", (String)"strip-prefix");

    public Substorage$fn__20013(Object object) {
        this.prefix = object;
    }

    public Object invoke(Object p1__20011_SHARP_) {
        Object object = p1__20011_SHARP_;
        p1__20011_SHARP_ = null;
        Substorage$fn__20013 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), this_.prefix), object);
    }
}

