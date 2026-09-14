/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.index;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index.DirNode;

public final class TreeIter$iter__15146__15152$fn__15153$iter__15148__15154$fn__15155$fn__15156
extends AFunction {
    Object c__6023__auto__;
    Object b__15151;
    Object ri;
    Object d;
    int size__6024__auto__;
    int ridx;
    int didx;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"not=");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-append");
    public static final Keyword const__6 = RT.keyword(null, (String)"key");
    public static final Keyword const__8 = RT.keyword(null, (String)"seg");
    public static final Keyword const__10 = RT.keyword(null, (String)"count");
    public static final Keyword const__11 = RT.keyword(null, (String)"ri");
    public static final Keyword const__12 = RT.keyword(null, (String)"di");

    public TreeIter$iter__15146__15152$fn__15153$iter__15148__15154$fn__15155$fn__15156(Object object, Object object2, Object object3, Object object4, int n, int n2, int n3) {
        this.c__6023__auto__ = object;
        this.b__15151 = object2;
        this.ri = object3;
        this.d = object4;
        this.size__6024__auto__ = n;
        this.ridx = n2;
        this.didx = n3;
    }

    public Object invoke() {
        long i__15150 = (int)0L;
        while (i__15150 < (long)this.size__6024__auto__) {
            Object object;
            Object or__5238__auto__15158;
            Object di = ((Indexed)this.c__6023__auto__).nth(RT.uncheckedIntCast((long)i__15150));
            Object object2 = or__5238__auto__15158 = ((IFn)const__3.getRawRoot()).invoke(this.ri, (Object)this.ridx);
            if (object2 != null && object2 != Boolean.FALSE) {
                object = or__5238__auto__15158;
                or__5238__auto__15158 = null;
            } else {
                object = Numbers.gte((Object)di, (long)this.didx) ? Boolean.TRUE : Boolean.FALSE;
            }
            if (object != null && object != Boolean.FALSE) {
                Object[] objectArray = new Object[10];
                objectArray[0] = const__6;
                objectArray[1] = RT.nth((Object)((DirNode)this.d).keydata, (int)RT.uncheckedIntCast((Object)((Number)di)));
                objectArray[2] = const__8;
                objectArray[3] = RT.aget((Object[])((Object[])((DirNode)this.d).segids), (int)RT.uncheckedIntCast((Object)di));
                objectArray[4] = const__10;
                objectArray[5] = RT.aget((int[])((int[])((DirNode)this.d).counts), (int)RT.uncheckedIntCast((Object)di));
                objectArray[6] = const__11;
                objectArray[7] = this.ri;
                objectArray[8] = const__12;
                Object object3 = di;
                di = null;
                objectArray[9] = object3;
                ((IFn)const__5.getRawRoot()).invoke(this.b__15151, (Object)RT.mapUniqueKeys((Object[])objectArray));
                ++i__15150;
                continue;
            }
            ++i__15150;
        }
        return Boolean.TRUE;
    }
}

