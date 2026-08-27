/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.LazySeq
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.LazySeq;
import datomic.datalog$compile_expr_clause$iter__18617__18621$fn__18622;

public final class datalog$compile_expr_clause$iter__18617__18621
extends AFunction {
    Object gret;
    Object vars;

    public datalog$compile_expr_clause$iter__18617__18621(Object object, Object object2) {
        this.gret = object;
        this.vars = object2;
    }

    public Object invoke(Object s__18618) {
        Object object = s__18618;
        s__18618 = null;
        return new LazySeq((IFn)new datalog$compile_expr_clause$iter__18617__18621$fn__18622((Object)this, this.gret, object, this.vars));
    }
}

