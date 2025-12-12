/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class datalog$push_preds$ctor__18558$fn__18565$fn__18566
extends AFunction {
    Object needs_source;
    Object f;
    Object args;
    Object consts;
    int arity;
    Object src;
    Object join_map;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"apply");

    public datalog$push_preds$ctor__18558$fn__18565$fn__18566(Object object, Object object2, Object object3, Object object4, int n, Object object5, Object object6) {
        this.needs_source = object;
        this.f = object2;
        this.args = object3;
        this.consts = object4;
        this.arity = n;
        this.src = object5;
        this.join_map = object6;
    }

    public Object invoke(Object y) {
        Object object;
        datalog$push_preds$ctor__18558$fn__18565$fn__18566 this_;
        long n__5742__auto__18568 = this_.arity;
        for (long i = 0L; i < n__5742__auto__18568; ++i) {
            Object object2 = ((IFn)const__5.getRawRoot()).invoke((Object)(Util.identical((Object)((IFn)this_.consts).invoke((Object)Numbers.num((long)i)), null) ? Boolean.TRUE : Boolean.FALSE));
            RT.aset((Object[])((Object[])this_.args), (int)((int)i), (Object)(object2 != null && object2 != Boolean.FALSE ? ((IFn)this_.consts).invoke((Object)Numbers.num((long)i)) : RT.nth((Object)y, (int)RT.uncheckedIntCast((Object)((Number)((IFn)this_.join_map).invoke((Object)Numbers.num((long)i)))))));
        }
        Object object3 = this_.needs_source;
        if (object3 != null && object3 != Boolean.FALSE) {
            this_ = null;
            object = ((IFn)const__9.getRawRoot()).invoke(this_.f, this_.src, this_.args);
        } else {
            this_ = null;
            object = ((IFn)const__9.getRawRoot()).invoke(this_.f, this_.args);
        }
        return object;
    }
}

