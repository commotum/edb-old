/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.lang.reflect.Method;

public final class datafy$get_java_method$fn__17314
extends AFunction {
    Object mname;
    Object types;
    Object arity;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"empty?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");

    public datafy$get_java_method$fn__17314(Object object, Object object2, Object object3) {
        this.mname = object;
        this.types = object2;
        this.arity = object3;
    }

    public Object invoke(Object p1__17313_SHARP_) {
        Boolean bl;
        boolean and__5236__auto__17317 = Util.equiv((Object)((IFn)const__1.getRawRoot()).invoke(this_.mname), (Object)((Method)p1__17313_SHARP_).getName());
        if (and__5236__auto__17317) {
            boolean and__5236__auto__17316 = Util.equiv((Object)this_.arity, (long)RT.count(((Method)p1__17313_SHARP_).getParameterTypes()));
            if (and__5236__auto__17316) {
                Object object = ((IFn)const__3.getRawRoot()).invoke(this_.types);
                if (object != null && object != Boolean.FALSE) {
                    bl = Boolean.TRUE;
                } else {
                    Object object2 = p1__17313_SHARP_;
                    p1__17313_SHARP_ = null;
                    datafy$get_java_method$fn__17314 this_ = null;
                    bl = Util.equiv((Object)((IFn)const__4.getRawRoot()).invoke(((Method)object2).getParameterTypes()), (Object)this_.types) ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__17316 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = and__5236__auto__17317 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }
}

