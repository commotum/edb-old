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
import datomic.iter.Iter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public final class datalog$fn__18233$join__18334
extends AFunction {
    Object predctor;
    Object hashx;
    Object match_QMARK_;
    Object project;
    Object ht;
    Object probe;
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"iterator");
    public static final Var const__1 = RT.var((String)"datomic.datalog", (String)"maybe-cancel");

    public datalog$fn__18233$join__18334(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.predctor = object;
        this.hashx = object2;
        this.match_QMARK_ = object3;
        this.project = object4;
        this.ht = object5;
        this.probe = object6;
    }

    public Object invoke(Object ps, Object ret) {
        Object pred2 = ((IFn)this.predctor).invoke();
        Object object = ps;
        ps = null;
        Object piter = ((IFn)const__0.getRawRoot()).invoke(object);
        while (((Iterator)piter).hasNext()) {
            Object p;
            ((IFn)const__1.getRawRoot()).invoke();
            Object e = p = ((Iterator)piter).next();
            p = null;
            Object diter = ((IFn)this.probe).invoke(e);
            while (true) {
                Object temp__5457__auto__18336;
                ((IFn)const__1.getRawRoot()).invoke();
                Object object2 = diter;
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object d = ((Iter)diter).get();
                Object v = temp__5457__auto__18336 = ((Map)this.ht).get(((IFn)this.hashx).invoke(d));
                if (v != null && v != Boolean.FALSE) {
                    Object ys;
                    Object v2 = temp__5457__auto__18336;
                    temp__5457__auto__18336 = null;
                    Object v3 = ys = v2;
                    ys = null;
                    Object yiter = ((IFn)const__0.getRawRoot()).invoke(v3);
                    while (((Iterator)yiter).hasNext()) {
                        Object y = ((Iterator)yiter).next();
                        Object object3 = ((IFn)this.match_QMARK_).invoke(d, y);
                        if (object3 == null || object3 == Boolean.FALSE) continue;
                        Object e2 = y;
                        y = null;
                        Object p2 = ((IFn)this.project).invoke(d, e2);
                        Object object4 = ((IFn)pred2).invoke(p2);
                        if (object4 == null || object4 == Boolean.FALSE) continue;
                        Object object5 = p2;
                        p2 = null;
                        Boolean bl = ((Collection)ret).add(object5) ? Boolean.TRUE : Boolean.FALSE;
                    }
                }
                Object object6 = diter;
                diter = null;
                diter = ((Iter)object6).next();
            }
        }
        return null;
    }
}

