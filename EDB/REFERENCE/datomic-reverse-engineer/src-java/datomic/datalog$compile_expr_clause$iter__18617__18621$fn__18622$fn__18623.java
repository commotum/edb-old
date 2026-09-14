/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class datalog$compile_expr_clause$iter__18617__18621$fn__18622$fn__18623
extends AFunction {
    Object c__6023__auto__;
    Object b__18620;
    Object gret;
    int size__6024__auto__;
    Object vars;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"chunk-append");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"clojure.core", (String)"aset");

    public datalog$compile_expr_clause$iter__18617__18621$fn__18622$fn__18623(Object object, Object object2, Object object3, int n, Object object4) {
        this.c__6023__auto__ = object;
        this.b__18620 = object2;
        this.gret = object3;
        this.size__6024__auto__ = n;
        this.vars = object4;
    }

    public Object invoke() {
        for (long i__18619 = (long)((int)0L); i__18619 < (long)this.size__6024__auto__; ++i__18619) {
            Object i = ((Indexed)this.c__6023__auto__).nth(RT.uncheckedIntCast((long)i__18619));
            Object object = ((IFn)const__6.getRawRoot()).invoke(i);
            Object object2 = i;
            i = null;
            ((IFn)const__3.getRawRoot()).invoke(this.b__18620, ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)const__7), ((IFn)const__6.getRawRoot()).invoke(this.gret), object, ((IFn)const__6.getRawRoot()).invoke(RT.nth((Object)this.vars, (int)RT.uncheckedIntCast((Object)((Number)object2)))))));
        }
        return Boolean.TRUE;
    }
}

