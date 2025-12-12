/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.index;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index.DirNode;
import datomic.index.TreeIter$iter__15146__15152$fn__15153$iter__15148__15154$fn__15155$fn__15156;

public final class TreeIter$iter__15146__15152$fn__15153$iter__15148__15154$fn__15155
extends AFunction {
    Object iter__15148;
    Object s__15149;
    Object ri;
    Object d;
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
    public static final Keyword const__13 = RT.keyword(null, (String)"key");
    public static final Keyword const__15 = RT.keyword(null, (String)"seg");
    public static final Keyword const__17 = RT.keyword(null, (String)"count");
    public static final Keyword const__18 = RT.keyword(null, (String)"ri");
    public static final Keyword const__19 = RT.keyword(null, (String)"di");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"rest");

    public TreeIter$iter__15146__15152$fn__15153$iter__15148__15154$fn__15155(Object object, Object object2, Object object3, Object object4, int n, int n2) {
        this.iter__15148 = object;
        this.s__15149 = object2;
        this.ri = object3;
        this.d = object4;
        this.ridx = n;
        this.didx = n2;
    }

    public Object invoke() {
        Object object;
        block7: {
            Object s__15149 = this_.s__15149 = null;
            while (true) {
                Object object2;
                Object or__5238__auto__15162;
                TreeIter$iter__15146__15152$fn__15153$iter__15148__15154$fn__15155 this_;
                Object temp__5457__auto__15163;
                Object object3 = s__15149;
                s__15149 = null;
                Object object4 = temp__5457__auto__15163 = ((IFn)const__0.getRawRoot()).invoke(object3);
                if (object4 == null || object4 == Boolean.FALSE) break;
                Object object5 = temp__5457__auto__15163;
                temp__5457__auto__15163 = null;
                Object s__151492 = object5;
                Object object6 = ((IFn)const__1.getRawRoot()).invoke(s__151492);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object c__6023__auto__15160 = ((IFn)const__2.getRawRoot()).invoke(s__151492);
                    int size__6024__auto__15161 = RT.count((Object)c__6023__auto__15160);
                    Object b__15151 = ((IFn)const__5.getRawRoot()).invoke((Object)size__6024__auto__15161);
                    Object object7 = c__6023__auto__15160;
                    c__6023__auto__15160 = null;
                    Object object8 = ((IFn)new TreeIter$iter__15146__15152$fn__15153$iter__15148__15154$fn__15155$fn__15156(object7, b__15151, this_.ri, this_.d, size__6024__auto__15161, this_.ridx, this_.didx)).invoke();
                    if (object8 != null && object8 != Boolean.FALSE) {
                        Object object9 = b__15151;
                        b__15151 = null;
                        Object object10 = s__151492;
                        s__151492 = null;
                        this_ = null;
                        object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object9), ((IFn)this_.iter__15148).invoke(((IFn)const__8.getRawRoot()).invoke(object10)));
                    } else {
                        Object object11 = b__15151;
                        b__15151 = null;
                        this_ = null;
                        object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object11), null);
                    }
                    break block7;
                }
                Object di = ((IFn)const__9.getRawRoot()).invoke(s__151492);
                Object object12 = or__5238__auto__15162 = ((IFn)const__10.getRawRoot()).invoke(this_.ri, (Object)this_.ridx);
                if (object12 != null && object12 != Boolean.FALSE) {
                    object2 = or__5238__auto__15162;
                    or__5238__auto__15162 = null;
                } else {
                    object2 = Numbers.gte((Object)di, (long)this_.didx) ? Boolean.TRUE : Boolean.FALSE;
                }
                if (object2 != null && object2 != Boolean.FALSE) {
                    Object[] objectArray = new Object[10];
                    objectArray[0] = const__13;
                    objectArray[1] = RT.nth((Object)((DirNode)this_.d).keydata, (int)RT.uncheckedIntCast((Object)((Number)di)));
                    objectArray[2] = const__15;
                    objectArray[3] = RT.aget((Object[])((Object[])((DirNode)this_.d).segids), (int)RT.uncheckedIntCast((Object)di));
                    objectArray[4] = const__17;
                    objectArray[5] = RT.aget((int[])((int[])((DirNode)this_.d).counts), (int)RT.uncheckedIntCast((Object)di));
                    objectArray[6] = const__18;
                    objectArray[7] = this_.ri;
                    objectArray[8] = const__19;
                    Object object13 = di;
                    di = null;
                    objectArray[9] = object13;
                    Object object14 = s__151492;
                    s__151492 = null;
                    this_ = null;
                    object = ((IFn)const__12.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), ((IFn)this_.iter__15148).invoke(((IFn)const__20.getRawRoot()).invoke(object14)));
                    break block7;
                }
                Object object15 = s__151492;
                s__151492 = null;
                s__15149 = ((IFn)const__20.getRawRoot()).invoke(object15);
            }
            object = null;
        }
        return object;
    }
}

