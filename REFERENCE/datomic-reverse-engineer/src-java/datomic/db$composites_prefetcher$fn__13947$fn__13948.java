/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.HashSet;

public final class db$composites_prefetcher$fn__13947$fn__13948
extends AFunction {
    Object dispatcher;
    long e;
    Object db;
    Object adder;
    Object needed_eas;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"prefetch-constituents");

    public db$composites_prefetcher$fn__13947$fn__13948(Object object, long l, Object object2, Object object3, Object object4) {
        this.dispatcher = object;
        this.e = l;
        this.db = object2;
        this.adder = object3;
        this.needed_eas = object4;
    }

    public Object invoke(Object p1__13946_SHARP_) {
        Object object;
        if (((HashSet)this_.needed_eas).add(Tuple.create((Object)Numbers.num((long)this_.e), (Object)p1__13946_SHARP_))) {
            Object object2 = p1__13946_SHARP_;
            p1__13946_SHARP_ = null;
            db$composites_prefetcher$fn__13947$fn__13948 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this_.dispatcher, this_.adder, this_.db, (Object)Numbers.num((long)this_.e), object2);
        } else {
            object = null;
        }
        return object;
    }
}

