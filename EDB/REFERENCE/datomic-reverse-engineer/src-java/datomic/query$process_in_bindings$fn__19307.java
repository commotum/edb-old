/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.query$process_in_bindings$fn__19307$fn__19312;
import datomic.query$process_in_bindings$fn__19307$fn__19314;

public final class query$process_in_bindings$fn__19307
extends AFunction {
    Object prefix;
    Object binding_QMARK_;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"datomic.datalog", (String)"binding-type");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"update-in");
    public static final AFn const__9 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"in"));
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"conj");
    public static final AFn const__12 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"where"));
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"vector");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__18 = (AFn)Symbol.intern(null, (String)"ground");
    public static final Keyword const__20 = RT.keyword(null, (String)"scalar");
    public static final AFn const__22 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"in-consts"));
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__24 = RT.keyword(null, (String)"tuple");
    public static final AFn const__25 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"in-consts"));
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"zipmap");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"range");
    public static final AFn const__30 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"in"));

    public query$process_in_bindings$fn__19307(Object object, Object object2) {
        this.prefix = object;
        this.binding_QMARK_ = object2;
    }

    public Object invoke(Object m, Object p__19306) {
        Object object;
        query$process_in_bindings$fn__19307 this_;
        Object object2 = p__19306;
        p__19306 = null;
        Object vec__19308 = object2;
        Object i = RT.nth((Object)vec__19308, (int)RT.intCast((long)0L), null);
        Object object3 = vec__19308;
        vec__19308 = null;
        Object x = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        Object object4 = ((IFn)this_.binding_QMARK_).invoke(x);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5;
            Object object6 = i;
            i = null;
            Object gs = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(this_.prefix, (Object)Numbers.inc((Object)object6)));
            Object bind_type = ((IFn)const__6.getRawRoot()).invoke(x);
            Object object7 = m;
            m = null;
            Object G__19311 = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object7, (Object)const__9, const__10.getRawRoot(), gs), (Object)const__12, (Object)new query$process_in_bindings$fn__19307$fn__19312(), ((IFn)const__13.getRawRoot()).invoke(const__14.getRawRoot(), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__18), ((IFn)const__17.getRawRoot()).invoke(gs)))), ((IFn)const__17.getRawRoot()).invoke(x)))));
            if (Util.equiv((Object)bind_type, (Object)const__20)) {
                Object object8 = G__19311;
                G__19311 = null;
                object5 = ((IFn)const__7.getRawRoot()).invoke(object8, (Object)const__22, const__23.getRawRoot(), x, gs);
            } else {
                object5 = G__19311;
                G__19311 = null;
            }
            Object G__193112 = object5;
            Object object9 = bind_type;
            bind_type = null;
            if (Util.equiv((Object)object9, (Object)const__24)) {
                Object object10 = G__193112;
                G__193112 = null;
                Object object11 = x;
                x = null;
                Object object12 = gs;
                gs = null;
                this_ = null;
                object = ((IFn)const__7.getRawRoot()).invoke(object10, (Object)const__25, const__26.getRawRoot(), ((IFn)const__27.getRawRoot()).invoke(object11, ((IFn)const__28.getRawRoot()).invoke((Object)new query$process_in_bindings$fn__19307$fn__19314(object12), ((IFn)const__29.getRawRoot()).invoke())));
            } else {
                object = G__193112;
                G__193112 = null;
            }
        } else {
            Object object13 = m;
            m = null;
            Object object14 = x;
            x = null;
            this_ = null;
            object = ((IFn)const__7.getRawRoot()).invoke(object13, (Object)const__30, const__10.getRawRoot(), object14);
        }
        return object;
    }
}

