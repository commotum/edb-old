/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.index;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index.DirNode;
import datomic.index.TreeIter$iter__15092__15098$fn__15099$iter__15094__15100$fn__15101$fn__15102;

public final class TreeIter$iter__15092__15098$fn__15099$iter__15094__15100$fn__15101
extends AFunction {
    Object d;
    Object iter__15094;
    Object ri;
    Object s__15095;
    int ridx;
    int didx;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-buffer");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-cons");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunk");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"not=");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"rest");

    public TreeIter$iter__15092__15098$fn__15099$iter__15094__15100$fn__15101(Object object, Object object2, Object object3, Object object4, int n, int n2) {
        this.d = object;
        this.iter__15094 = object2;
        this.ri = object3;
        this.s__15095 = object4;
        this.ridx = n;
        this.didx = n2;
    }

    public Object invoke() {
        Object object;
        block7: {
            Object s__15095 = this_.s__15095 = null;
            while (true) {
                Object object2;
                Object or__5238__auto__15108;
                TreeIter$iter__15092__15098$fn__15099$iter__15094__15100$fn__15101 this_;
                Object temp__5457__auto__15109;
                Object object3 = s__15095;
                s__15095 = null;
                Object object4 = temp__5457__auto__15109 = ((IFn)const__0.getRawRoot()).invoke(object3);
                if (object4 == null || object4 == Boolean.FALSE) break;
                Object object5 = temp__5457__auto__15109;
                temp__5457__auto__15109 = null;
                Object s__150952 = object5;
                Object object6 = ((IFn)const__1.getRawRoot()).invoke(s__150952);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object c__6023__auto__15106 = ((IFn)const__2.getRawRoot()).invoke(s__150952);
                    int size__6024__auto__15107 = RT.count((Object)c__6023__auto__15106);
                    Object b__15097 = ((IFn)const__5.getRawRoot()).invoke((Object)size__6024__auto__15107);
                    Object object7 = c__6023__auto__15106;
                    c__6023__auto__15106 = null;
                    Object object8 = ((IFn)new TreeIter$iter__15092__15098$fn__15099$iter__15094__15100$fn__15101$fn__15102(size__6024__auto__15107, this_.d, this_.ri, this_.ridx, this_.didx, object7, b__15097)).invoke();
                    if (object8 != null && object8 != Boolean.FALSE) {
                        Object object9 = b__15097;
                        b__15097 = null;
                        Object object10 = s__150952;
                        s__150952 = null;
                        this_ = null;
                        object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object9), ((IFn)this_.iter__15094).invoke(((IFn)const__8.getRawRoot()).invoke(object10)));
                    } else {
                        Object object11 = b__15097;
                        b__15097 = null;
                        this_ = null;
                        object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object11), null);
                    }
                    break block7;
                }
                Object di = ((IFn)const__9.getRawRoot()).invoke(s__150952);
                Object object12 = or__5238__auto__15108 = ((IFn)const__10.getRawRoot()).invoke(this_.ri, (Object)this_.ridx);
                if (object12 != null && object12 != Boolean.FALSE) {
                    object2 = or__5238__auto__15108;
                    or__5238__auto__15108 = null;
                } else {
                    object2 = Numbers.gte((Object)di, (long)this_.didx) ? Boolean.TRUE : Boolean.FALSE;
                }
                if (object2 != null && object2 != Boolean.FALSE) {
                    Object object13 = di;
                    di = null;
                    Object object14 = s__150952;
                    s__150952 = null;
                    this_ = null;
                    object = ((IFn)const__12.getRawRoot()).invoke(RT.nth((Object)((DirNode)this_.d).segids, (int)RT.uncheckedIntCast((Object)((Number)object13))), ((IFn)this_.iter__15094).invoke(((IFn)const__14.getRawRoot()).invoke(object14)));
                    break block7;
                }
                Object object15 = s__150952;
                s__150952 = null;
                s__15095 = ((IFn)const__14.getRawRoot()).invoke(object15);
            }
            object = null;
        }
        return object;
    }
}

