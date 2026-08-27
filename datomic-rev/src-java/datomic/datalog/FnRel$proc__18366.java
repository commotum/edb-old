/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.datalog;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.ArrayList;
import java.util.Iterator;

public final class FnRel$proc__18366
extends AFunction {
    Object predctor;
    Object consts;
    Object project;
    Object f;
    long PART;
    Object db;
    Object join_map;
    Object arity;
    Object match_QMARK_;
    Object src_QMARK_;
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"iterator");
    public static final Var const__2 = RT.var((String)"datomic.datalog", (String)"maybe-cancel");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"apply");

    public FnRel$proc__18366(Object object, Object object2, Object object3, Object object4, long l, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.predctor = object;
        this.consts = object2;
        this.project = object3;
        this.f = object4;
        this.PART = l;
        this.db = object5;
        this.join_map = object6;
        this.arity = object7;
        this.match_QMARK_ = object8;
        this.src_QMARK_ = object9;
    }

    public Object invoke(Object ys) {
        Object pred2 = ((IFn)this.predctor).invoke();
        Object object = ys;
        ys = null;
        Object yiter = ((IFn)const__0.getRawRoot()).invoke(object);
        ArrayList<Object> rel = new ArrayList<Object>(RT.uncheckedIntCast((long)this.PART));
        Object[] args = RT.object_array((Object)this.arity);
        ((IFn)const__2.getRawRoot()).invoke();
        while (((Iterator)yiter).hasNext()) {
            Object y = ((Iterator)yiter).next();
            long n__5742__auto__18368 = RT.longCast((Object)this.arity);
            for (long i = 0L; i < n__5742__auto__18368; ++i) {
                Object object2 = ((IFn)const__8.getRawRoot()).invoke((Object)(Util.identical((Object)((IFn)this.consts).invoke((Object)Numbers.num((long)i)), null) ? Boolean.TRUE : Boolean.FALSE));
                RT.aset((Object[])args, (int)((int)i), (Object)(object2 != null && object2 != Boolean.FALSE ? ((IFn)this.consts).invoke((Object)Numbers.num((long)i)) : RT.nth(y, (int)RT.uncheckedIntCast((Object)((Number)((IFn)this.join_map).invoke((Object)Numbers.num((long)i)))))));
            }
            Object object3 = this.src_QMARK_;
            Object xiter = ((IFn)const__0.getRawRoot()).invoke(object3 != null && object3 != Boolean.FALSE ? ((IFn)const__12.getRawRoot()).invoke(this.f, this.db, (Object)args) : ((IFn)const__12.getRawRoot()).invoke(this.f, (Object)args));
            while (((Iterator)xiter).hasNext()) {
                Object x = ((Iterator)xiter).next();
                Object object4 = ((IFn)this.match_QMARK_).invoke(x, y);
                if (object4 == null || object4 == Boolean.FALSE) continue;
                Object e = x;
                x = null;
                Object p = ((IFn)this.project).invoke(e, y);
                Object object5 = ((IFn)pred2).invoke(p);
                if (object5 == null || object5 == Boolean.FALSE) continue;
                Object object6 = p;
                p = null;
                Boolean bl = rel.add(object6) ? Boolean.TRUE : Boolean.FALSE;
            }
        }
        ArrayList<Object> arrayList = rel;
        rel = null;
        return arrayList;
    }
}

