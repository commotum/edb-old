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

public final class common$distinct_by$step__9247$fn__9248$fn__9250
extends AFunction {
    Object f;
    Object step;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"rest");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"conj");

    public common$distinct_by$step__9247$fn__9248$fn__9250(Object object, Object object2) {
        this.f = object;
        this.step = object2;
    }

    public Object invoke(Object p__9249, Object seen) {
        Object object;
        block2: {
            block1: {
                Object k;
                Object s;
                Object fst;
                while (true) {
                    Object temp__5457__auto__9255;
                    Object xs;
                    Object object2 = p__9249;
                    p__9249 = null;
                    Object vec__9251 = object2;
                    fst = RT.nth((Object)vec__9251, (int)RT.uncheckedIntCast((long)0L), null);
                    Object object3 = vec__9251;
                    vec__9251 = null;
                    Object object4 = xs = object3;
                    xs = null;
                    Object object5 = temp__5457__auto__9255 = ((IFn)const__2.getRawRoot()).invoke(object4);
                    if (object5 == null || object5 == Boolean.FALSE) break block1;
                    Object object6 = temp__5457__auto__9255;
                    temp__5457__auto__9255 = null;
                    s = object6;
                    k = ((IFn)this_.f).invoke(fst);
                    Object object7 = ((IFn)const__3.getRawRoot()).invoke(seen, k);
                    if (object7 == null || object7 == Boolean.FALSE) break;
                    Object object8 = s;
                    s = null;
                    Object object9 = seen;
                    seen = null;
                    seen = object9;
                    p__9249 = ((IFn)const__4.getRawRoot()).invoke(object8);
                }
                Object object10 = fst;
                fst = null;
                Object object11 = s;
                s = null;
                Object object12 = seen;
                seen = null;
                Object object13 = k;
                k = null;
                common$distinct_by$step__9247$fn__9248$fn__9250 this_ = null;
                object = ((IFn)const__5.getRawRoot()).invoke(object10, ((IFn)this_.step).invoke(((IFn)const__4.getRawRoot()).invoke(object11), ((IFn)const__6.getRawRoot()).invoke(object12, object13)));
                break block2;
            }
            object = null;
        }
        return object;
    }
}

