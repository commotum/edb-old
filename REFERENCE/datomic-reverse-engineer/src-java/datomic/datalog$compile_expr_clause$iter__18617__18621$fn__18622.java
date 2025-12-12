/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.datalog$compile_expr_clause$iter__18617__18621$fn__18622$fn__18623;

public final class datalog$compile_expr_clause$iter__18617__18621$fn__18622
extends AFunction {
    Object iter__18617;
    Object gret;
    Object s__18618;
    Object vars;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-buffer");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-cons");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunk");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__13 = (AFn)Symbol.intern((String)"clojure.core", (String)"aset");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"rest");

    public datalog$compile_expr_clause$iter__18617__18621$fn__18622(Object object, Object object2, Object object3, Object object4) {
        this.iter__18617 = object;
        this.gret = object2;
        this.s__18618 = object3;
        this.vars = object4;
    }

    public Object invoke() {
        Object object;
        Object temp__5457__auto__18628;
        Object s__18618;
        Object object2 = s__18618 = (this_.s__18618 = null);
        s__18618 = null;
        Object object3 = temp__5457__auto__18628 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            datalog$compile_expr_clause$iter__18617__18621$fn__18622 this_;
            Object object4 = temp__5457__auto__18628;
            temp__5457__auto__18628 = null;
            Object s__186182 = object4;
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(s__186182);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object c__6023__auto__18626 = ((IFn)const__2.getRawRoot()).invoke(s__186182);
                int size__6024__auto__18627 = RT.count((Object)c__6023__auto__18626);
                Object b__18620 = ((IFn)const__5.getRawRoot()).invoke((Object)size__6024__auto__18627);
                Object object6 = c__6023__auto__18626;
                c__6023__auto__18626 = null;
                Object object7 = ((IFn)new datalog$compile_expr_clause$iter__18617__18621$fn__18622$fn__18623(object6, b__18620, this_.gret, size__6024__auto__18627, this_.vars)).invoke();
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = b__18620;
                    b__18620 = null;
                    Object object9 = s__186182;
                    s__186182 = null;
                    this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object8), ((IFn)this_.iter__18617).invoke(((IFn)const__8.getRawRoot()).invoke(object9)));
                } else {
                    Object object10 = b__18620;
                    b__18620 = null;
                    this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object10), null);
                }
            } else {
                Object i = ((IFn)const__9.getRawRoot()).invoke(s__186182);
                Object object11 = ((IFn)const__12.getRawRoot()).invoke(i);
                Object object12 = i;
                i = null;
                Object object13 = s__186182;
                s__186182 = null;
                this_ = null;
                object = ((IFn)const__10.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke((Object)const__13), ((IFn)const__12.getRawRoot()).invoke(this_.gret), object11, ((IFn)const__12.getRawRoot()).invoke(RT.nth((Object)this_.vars, (int)RT.uncheckedIntCast((Object)((Number)object12)))))), ((IFn)this_.iter__18617).invoke(((IFn)const__15.getRawRoot()).invoke(object13)));
            }
        } else {
            object = null;
        }
        return object;
    }
}

