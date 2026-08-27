/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  clojure.lang.Volatile
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import clojure.lang.Volatile;

public final class log$partition_by_weight$fn__16529$fn__16530
extends AFunction {
    Object target;
    Object rf;
    Object weigh;
    Object weight_ref;
    Object part_ref;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"vreset!");
    public static final Object const__6 = 0L;

    public log$partition_by_weight$fn__16529$fn__16530(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.target = object;
        this.rf = object2;
        this.weigh = object3;
        this.weight_ref = object4;
        this.part_ref = object5;
    }

    public Object invoke(Object result2, Object input) {
        Object object;
        Object weight = ((Volatile)this_.weight_ref).reset((Object)Numbers.add((Object)((Volatile)this_.weight_ref).deref(), (Object)((IFn)this_.weigh).invoke(input)));
        Object object2 = input;
        input = null;
        Object part2 = ((Volatile)this_.part_ref).reset(((IFn)const__3.getRawRoot()).invoke(((Volatile)this_.part_ref).deref(), object2));
        Object object3 = weight;
        weight = null;
        if (Numbers.lt((Object)object3, (Object)this_.target)) {
            object = result2;
            result2 = null;
        } else {
            ((IFn)const__5.getRawRoot()).invoke(this_.weight_ref, const__6);
            ((IFn)const__5.getRawRoot()).invoke(this_.part_ref, (Object)PersistentVector.EMPTY);
            Object object4 = result2;
            result2 = null;
            Object object5 = part2;
            part2 = null;
            log$partition_by_weight$fn__16529$fn__16530 this_ = null;
            object = ((IFn)this_.rf).invoke(object4, object5);
        }
        return object;
    }

    public Object invoke(Object result2) {
        Object object;
        log$partition_by_weight$fn__16529$fn__16530 this_;
        Object temp__5455__auto__16532;
        Object object2 = temp__5455__auto__16532 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this_.part_ref));
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5455__auto__16532;
            temp__5455__auto__16532 = null;
            Object part2 = object3;
            Object object4 = result2;
            result2 = null;
            Object object5 = part2;
            part2 = null;
            this_ = null;
            object = ((IFn)this_.rf).invoke(((IFn)this_.rf).invoke(object4, object5));
        } else {
            Object object6 = result2;
            result2 = null;
            this_ = null;
            object = ((IFn)this_.rf).invoke(object6);
        }
        return object;
    }

    public Object invoke() {
        log$partition_by_weight$fn__16529$fn__16530 this_ = null;
        return ((IFn)this_.rf).invoke();
    }
}

