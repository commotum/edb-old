/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.index;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index.DirNode;
import datomic.index.Index$iter__15197__15203$fn__15204$iter__15199__15205$fn__15206$fn__15207;

public final class Index$iter__15197__15203$fn__15204$iter__15199__15205$fn__15206
extends AFunction {
    Object s__15200;
    Object d;
    Object iter__15199;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-buffer");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-cons");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunk");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"cons");
    public static final Keyword const__11 = RT.keyword(null, (String)"key");
    public static final Keyword const__13 = RT.keyword(null, (String)"seg");
    public static final Keyword const__15 = RT.keyword(null, (String)"count");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"rest");

    public Index$iter__15197__15203$fn__15204$iter__15199__15205$fn__15206(Object object, Object object2, Object object3) {
        this.s__15200 = object;
        this.d = object2;
        this.iter__15199 = object3;
    }

    public Object invoke() {
        Object object;
        Object temp__5457__auto__15212;
        Object s__15200;
        Object object2 = s__15200 = (this_.s__15200 = null);
        s__15200 = null;
        Object object3 = temp__5457__auto__15212 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Index$iter__15197__15203$fn__15204$iter__15199__15205$fn__15206 this_;
            Object object4 = temp__5457__auto__15212;
            temp__5457__auto__15212 = null;
            Object s__152002 = object4;
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(s__152002);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object c__6023__auto__15210 = ((IFn)const__2.getRawRoot()).invoke(s__152002);
                int size__6024__auto__15211 = RT.count((Object)c__6023__auto__15210);
                Object b__15202 = ((IFn)const__5.getRawRoot()).invoke((Object)size__6024__auto__15211);
                Object object6 = c__6023__auto__15210;
                c__6023__auto__15210 = null;
                Object object7 = ((IFn)new Index$iter__15197__15203$fn__15204$iter__15199__15205$fn__15206$fn__15207(object6, b__15202, this_.d, size__6024__auto__15211)).invoke();
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = b__15202;
                    b__15202 = null;
                    Object object9 = s__152002;
                    s__152002 = null;
                    this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object8), ((IFn)this_.iter__15199).invoke(((IFn)const__8.getRawRoot()).invoke(object9)));
                } else {
                    Object object10 = b__15202;
                    b__15202 = null;
                    this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object10), null);
                }
            } else {
                Object di = ((IFn)const__9.getRawRoot()).invoke(s__152002);
                Object[] objectArray = new Object[6];
                objectArray[0] = const__11;
                objectArray[1] = RT.nth((Object)((DirNode)this_.d).keydata, (int)RT.uncheckedIntCast((Object)((Number)di)));
                objectArray[2] = const__13;
                objectArray[3] = RT.aget((Object[])((Object[])((DirNode)this_.d).segids), (int)RT.uncheckedIntCast((Object)di));
                objectArray[4] = const__15;
                Object object11 = di;
                di = null;
                objectArray[5] = RT.aget((int[])((int[])((DirNode)this_.d).counts), (int)RT.uncheckedIntCast((Object)object11));
                Object object12 = s__152002;
                s__152002 = null;
                this_ = null;
                object = ((IFn)const__10.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), ((IFn)this_.iter__15199).invoke(((IFn)const__16.getRawRoot()).invoke(object12)));
            }
        } else {
            object = null;
        }
        return object;
    }
}

