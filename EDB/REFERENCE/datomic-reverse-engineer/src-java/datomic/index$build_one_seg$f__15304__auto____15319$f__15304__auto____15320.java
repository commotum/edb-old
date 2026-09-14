/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class index$build_one_seg$f__15304__auto____15319$f__15304__auto____15320
extends AFunction {
    Object proc;
    Object prev;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"compare");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"next");

    public index$build_one_seg$f__15304__auto____15319$f__15304__auto____15320(Object object, Object object2) {
        this.proc = object;
        this.prev = object2;
    }

    public Object invoke(Object data2, Object cnt, Object i) {
        while (true) {
            Object object;
            Object and__5236__auto__15324;
            Object object2 = and__5236__auto__15324 = data2;
            if (object2 != null && object2 != Boolean.FALSE) {
                Object d = ((IFn)const__0.getRawRoot()).invoke(data2);
                boolean and__5236__auto__15323 = Util.equiv((long)((IDatum)this.prev).getE(), (long)((IDatum)d).getE());
                if (and__5236__auto__15323) {
                    boolean and__5236__auto__15322 = Util.equiv((long)((IDatum)this.prev).getA(), (long)((IDatum)d).getA());
                    if (and__5236__auto__15322) {
                        Object object3 = d;
                        d = null;
                        object = Numbers.isZero((long)((IFn.OOL)const__3.getRawRoot()).invokePrim(((IDatum)this.prev).getV(), ((IDatum)object3).getV())) ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        object = and__5236__auto__15322 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    object = and__5236__auto__15323 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object = and__5236__auto__15324;
                and__5236__auto__15324 = null;
            }
            if (object == null || object == Boolean.FALSE) break;
            ((IFn)this.proc).invoke(((IFn)const__0.getRawRoot()).invoke(data2));
            Object object4 = data2;
            data2 = null;
            Object object5 = cnt;
            cnt = null;
            Object object6 = i;
            i = null;
            i = Numbers.unchecked_inc((Object)object6);
            cnt = Numbers.unchecked_dec((Object)object5);
            data2 = ((IFn)const__4.getRawRoot()).invoke(object4);
        }
        Object object = data2;
        data2 = null;
        Object object7 = cnt;
        cnt = null;
        Object object8 = i;
        i = null;
        return Tuple.create((Object)object, (Object)object7, (Object)object8);
    }
}

