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
import datomic.db$composites_prefetcher$fn__13947$fn__13948;
import datomic.impl.db.IDatum;

public final class db$composites_prefetcher$fn__13947
extends AFunction {
    Object dispatcher;
    Object constituents;
    Object db;
    Object adder;
    Object needed_eas;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"run!");

    public db$composites_prefetcher$fn__13947(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.dispatcher = object;
        this.constituents = object2;
        this.db = object3;
        this.adder = object4;
        this.needed_eas = object5;
    }

    public Object invoke(Object d) {
        Object object;
        Object temp__5457__auto__13951;
        long e = ((IDatum)d).getE();
        Object object2 = d;
        d = null;
        int a = ((IDatum)object2).getA();
        Object object3 = temp__5457__auto__13951 = RT.get((Object)this_.constituents, (Object)a);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object composites;
            Object object4 = temp__5457__auto__13951;
            temp__5457__auto__13951 = null;
            Object object5 = composites = object4;
            composites = null;
            db$composites_prefetcher$fn__13947 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)new db$composites_prefetcher$fn__13947$fn__13948(this_.dispatcher, e, this_.db, this_.adder, this_.needed_eas), object5);
        } else {
            object = null;
        }
        return object;
    }
}

