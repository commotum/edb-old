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

public final class query$process_ranges$norm__19325
extends AFunction {
    Object const_QMARK_;
    Object swap;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"list");

    public query$process_ranges$norm__19325(Object object, Object object2) {
        this.const_QMARK_ = object;
        this.swap = object2;
    }

    public Object invoke(Object p__19324) {
        Object object;
        Object object2;
        Object and__5236__auto__19333;
        Object vec__19326;
        Object object3 = p__19324;
        p__19324 = null;
        Object object4 = vec__19326 = object3;
        vec__19326 = null;
        Object vec__19329 = RT.nth((Object)object4, (int)RT.intCast((long)0L), null);
        Object cmp = RT.nth((Object)vec__19329, (int)RT.intCast((long)0L), null);
        Object x = RT.nth((Object)vec__19329, (int)RT.intCast((long)1L), null);
        Object y = RT.nth((Object)vec__19329, (int)RT.intCast((long)2L), null);
        Object object5 = vec__19329;
        vec__19329 = null;
        Object c = object5;
        Object object6 = and__5236__auto__19333 = ((IFn)const__4.getRawRoot()).invoke(((IFn)this_.const_QMARK_).invoke(x));
        if (object6 != null && object6 != Boolean.FALSE) {
            object2 = ((IFn)this_.const_QMARK_).invoke(y);
        } else {
            object2 = and__5236__auto__19333;
            and__5236__auto__19333 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = c;
            c = null;
        } else {
            Object object7;
            Object and__5236__auto__19334;
            Object object8 = and__5236__auto__19334 = ((IFn)this_.const_QMARK_).invoke(x);
            if (object8 != null && object8 != Boolean.FALSE) {
                object7 = ((IFn)const__4.getRawRoot()).invoke(((IFn)this_.const_QMARK_).invoke(y));
            } else {
                object7 = and__5236__auto__19334;
                and__5236__auto__19334 = null;
            }
            if (object7 != null && object7 != Boolean.FALSE) {
                Object object9 = cmp;
                cmp = null;
                Object object10 = y;
                y = null;
                Object object11 = x;
                x = null;
                query$process_ranges$norm__19325 this_ = null;
                object = ((IFn)const__5.getRawRoot()).invoke(((IFn)this_.swap).invoke(object9), object10, object11);
            } else {
                object = null;
            }
        }
        return object;
    }
}

