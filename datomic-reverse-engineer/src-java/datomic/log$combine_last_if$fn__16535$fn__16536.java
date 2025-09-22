/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class log$combine_last_if$fn__16535$fn__16536
extends AFunction {
    Object rf;
    Object combine;
    Object tail_ref;
    Object pred;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Keyword const__4 = RT.keyword(null, (String)"finish");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"vreset!");

    public log$combine_last_if$fn__16535$fn__16536(Object object, Object object2, Object object3, Object object4) {
        this.rf = object;
        this.combine = object2;
        this.tail_ref = object3;
        this.pred = object4;
    }

    public Object invoke(Object result2, Object input) {
        Object object;
        Object p2;
        Object vec__16541 = ((IFn)const__0.getRawRoot()).invoke(this_.tail_ref);
        Object p1 = RT.nth((Object)vec__16541, (int)RT.intCast((long)0L), null);
        Object object2 = vec__16541;
        vec__16541 = null;
        Object object3 = p2 = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        p2 = null;
        Object object4 = input;
        input = null;
        ((IFn)const__5.getRawRoot()).invoke(this_.tail_ref, (Object)Tuple.create((Object)object3, (Object)object4));
        Object object5 = p1;
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = result2;
            result2 = null;
            Object object7 = p1;
            p1 = null;
            log$combine_last_if$fn__16535$fn__16536 this_ = null;
            object = ((IFn)this_.rf).invoke(object6, object7);
        } else {
            object = result2;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object result2) {
        Object object;
        log$combine_last_if$fn__16535$fn__16536 this_;
        Object vec__16537 = ((IFn)const__0.getRawRoot()).invoke(this_.tail_ref);
        Object p1 = RT.nth((Object)vec__16537, (int)RT.intCast((long)0L), null);
        Object object2 = vec__16537;
        vec__16537 = null;
        Object p2 = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = p1;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = ((IFn)this_.pred).invoke(p2);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = result2;
                result2 = null;
                Object object6 = p1;
                p1 = null;
                Object object7 = p2;
                p2 = null;
                this_ = null;
                object = ((IFn)this_.rf).invoke(((IFn)this_.rf).invoke(object5, ((IFn)this_.combine).invoke(object6, object7)));
            } else {
                Object object8 = result2;
                result2 = null;
                Object object9 = p1;
                p1 = null;
                Object object10 = p2;
                p2 = null;
                this_ = null;
                object = ((IFn)this_.rf).invoke(((IFn)this_.rf).invoke(((IFn)this_.rf).invoke(object8, object9), object10));
            }
        } else {
            Object object11;
            Object object12 = result2;
            result2 = null;
            Object G__16540 = object12;
            Object object13 = p2;
            if (object13 != null && object13 != Boolean.FALSE) {
                Object object14 = G__16540;
                G__16540 = null;
                Object object15 = p2;
                p2 = null;
                object11 = ((IFn)this_.rf).invoke(object14, object15);
            } else {
                object11 = G__16540;
                G__16540 = null;
            }
            Object G__165402 = object11;
            Keyword keyword = const__4;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object object16 = G__165402;
                G__165402 = null;
                this_ = null;
                object = ((IFn)this_.rf).invoke(object16);
            } else {
                object = G__165402;
                G__165402 = null;
            }
        }
        return object;
    }

    public Object invoke() {
        log$combine_last_if$fn__16535$fn__16536 this_ = null;
        return ((IFn)this_.rf).invoke();
    }
}

