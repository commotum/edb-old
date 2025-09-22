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
import datomic.index.TreeIter$iter__15121__15127$fn__15128$iter__15123__15129$fn__15130$fn__15131;

public final class TreeIter$iter__15121__15127$fn__15128$iter__15123__15129$fn__15130
extends AFunction {
    Object s__15124;
    Object ri;
    Object lookup;
    Object d;
    Object iter__15123;
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
    public static final Var const__13 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"rest");

    public TreeIter$iter__15121__15127$fn__15128$iter__15123__15129$fn__15130(Object object, Object object2, Object object3, Object object4, Object object5, int n, int n2) {
        this.s__15124 = object;
        this.ri = object2;
        this.lookup = object3;
        this.d = object4;
        this.iter__15123 = object5;
        this.ridx = n;
        this.didx = n2;
    }

    public Object invoke() {
        Object object;
        block7: {
            Object s__15124 = this_.s__15124 = null;
            while (true) {
                Object object2;
                Object or__5238__auto__15137;
                TreeIter$iter__15121__15127$fn__15128$iter__15123__15129$fn__15130 this_;
                Object temp__5457__auto__15138;
                Object object3 = s__15124;
                s__15124 = null;
                Object object4 = temp__5457__auto__15138 = ((IFn)const__0.getRawRoot()).invoke(object3);
                if (object4 == null || object4 == Boolean.FALSE) break;
                Object object5 = temp__5457__auto__15138;
                temp__5457__auto__15138 = null;
                Object s__151242 = object5;
                Object object6 = ((IFn)const__1.getRawRoot()).invoke(s__151242);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object c__6023__auto__15135 = ((IFn)const__2.getRawRoot()).invoke(s__151242);
                    int size__6024__auto__15136 = RT.count((Object)c__6023__auto__15135);
                    Object b__15126 = ((IFn)const__5.getRawRoot()).invoke((Object)size__6024__auto__15136);
                    Object object7 = c__6023__auto__15135;
                    c__6023__auto__15135 = null;
                    Object object8 = ((IFn)new TreeIter$iter__15121__15127$fn__15128$iter__15123__15129$fn__15130$fn__15131(object7, this_.ri, this_.lookup, size__6024__auto__15136, b__15126, this_.d, this_.ridx, this_.didx)).invoke();
                    if (object8 != null && object8 != Boolean.FALSE) {
                        Object object9 = b__15126;
                        b__15126 = null;
                        Object object10 = s__151242;
                        s__151242 = null;
                        this_ = null;
                        object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object9), ((IFn)this_.iter__15123).invoke(((IFn)const__8.getRawRoot()).invoke(object10)));
                    } else {
                        Object object11 = b__15126;
                        b__15126 = null;
                        this_ = null;
                        object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object11), null);
                    }
                    break block7;
                }
                Object di = ((IFn)const__9.getRawRoot()).invoke(s__151242);
                Object object12 = or__5238__auto__15137 = ((IFn)const__10.getRawRoot()).invoke(this_.ri, (Object)this_.ridx);
                if (object12 != null && object12 != Boolean.FALSE) {
                    object2 = or__5238__auto__15137;
                    or__5238__auto__15137 = null;
                } else {
                    object2 = Numbers.gte((Object)di, (long)this_.didx) ? Boolean.TRUE : Boolean.FALSE;
                }
                if (object2 != null && object2 != Boolean.FALSE) {
                    IFn iFn = (IFn)const__12.getRawRoot();
                    ((IFn)const__13.getRawRoot()).invoke(this_.lookup, RT.nth((Object)((DirNode)this_.d).segids, (int)RT.uncheckedIntCast((Object)((Number)di))));
                    Object object13 = di;
                    di = null;
                    Object object14 = s__151242;
                    s__151242 = null;
                    this_ = null;
                    object = iFn.invoke(RT.nth((Object)((DirNode)this_.d).keydata, (int)RT.uncheckedIntCast((Object)((Number)object13))), ((IFn)this_.iter__15123).invoke(((IFn)const__15.getRawRoot()).invoke(object14)));
                    break block7;
                }
                Object object15 = s__151242;
                s__151242 = null;
                s__15124 = ((IFn)const__15.getRawRoot()).invoke(object15);
            }
            object = null;
        }
        return object;
    }
}

