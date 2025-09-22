/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog.DbRel;

public final class datalog$fn__18233$bind__18245
extends AFunction {
    Object bindings;
    Object const_attrid;
    Object consts;
    Object dbrel;
    Object db;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Object const__3 = 4L;
    public static final Keyword const__7 = RT.keyword(null, (String)"else");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"retracting-datum");
    public static final Object const__10 = 0L;
    public static final Var const__11 = RT.var((String)"datomic.datalog", (String)"resolve-id");
    public static final Object const__13 = -1L;
    public static final Object const__14 = 2L;
    public static final Var const__15 = RT.var((String)"datomic.db", (String)"normalize-kw");
    public static final Object const__16 = 3L;
    public static final Object const__17 = 0x1FFFFFFFFFFFFFFFL;

    public datalog$fn__18233$bind__18245(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.bindings = object;
        this.const_attrid = object2;
        this.consts = object3;
        this.dbrel = object4;
        this.db = object5;
    }

    public Object invoke(Object y) {
        Object object;
        Object or__5238__auto__18250;
        Object object2;
        Object temp__5457__auto__18249;
        Object object3;
        Object v;
        Object object4;
        Object object5;
        Object or__5238__auto__18248;
        Object object6;
        Object or__5238__auto__18247;
        Object added;
        Object object7;
        Object object8 = ((IFn)const__0.getRawRoot()).invoke((Object)(Util.identical((Object)RT.get((Object)this_.consts, (Object)const__3), null) ? Boolean.TRUE : Boolean.FALSE));
        if (object8 != null && object8 != Boolean.FALSE) {
            object7 = RT.get((Object)this_.consts, (Object)const__3);
        } else {
            Object object9 = RT.aget((Object[])((Object[])this_.bindings), (int)((int)4L));
            if (object9 != null && object9 != Boolean.FALSE) {
                object7 = RT.nth((Object)y, (int)RT.uncheckedIntCast((Object)((Number)RT.aget((Object[])((Object[])this_.bindings), (int)((int)4L)))));
            } else {
                Keyword keyword = const__7;
                object7 = keyword != null && keyword != Boolean.FALSE ? Boolean.TRUE : null;
            }
        }
        Object object10 = added = object7;
        added = null;
        IFn iFn = (IFn)(object10 != null && object10 != Boolean.FALSE ? const__8.getRawRoot() : const__9.getRawRoot());
        Object object11 = or__5238__auto__18247 = RT.get((Object)this_.consts, (Object)const__10);
        if (object11 != null && object11 != Boolean.FALSE) {
            object6 = or__5238__auto__18247;
            or__5238__auto__18247 = null;
        } else {
            Object object12 = RT.aget((Object[])((Object[])this_.bindings), (int)((int)0L));
            object6 = object12 != null && object12 != Boolean.FALSE ? ((IFn)const__11.getRawRoot()).invoke(this_.db, RT.nth((Object)y, (int)RT.uncheckedIntCast((Object)((Number)RT.aget((Object[])((Object[])this_.bindings), (int)((int)0L)))))) : Numbers.num((long)Long.MIN_VALUE);
        }
        Object object13 = or__5238__auto__18248 = this_.const_attrid;
        if (object13 != null && object13 != Boolean.FALSE) {
            object5 = or__5238__auto__18248;
            or__5238__auto__18248 = null;
        } else {
            Object object14 = RT.aget((Object[])((Object[])this_.bindings), (int)((int)1L));
            object5 = object14 != null && object14 != Boolean.FALSE ? ((IFn)const__11.getRawRoot()).invoke(this_.db, RT.nth((Object)y, (int)RT.uncheckedIntCast((Object)((Number)RT.aget((Object[])((Object[])this_.bindings), (int)((int)1L)))))) : const__13;
        }
        Object cv = RT.get((Object)this_.consts, (Object)const__14);
        if (Util.identical((Object)cv, null)) {
            Object object15 = RT.aget((Object[])((Object[])this_.bindings), (int)((int)2L));
            object4 = object15 != null && object15 != Boolean.FALSE ? RT.nth((Object)y, (int)RT.uncheckedIntCast((Object)((Number)RT.aget((Object[])((Object[])this_.bindings), (int)((int)2L))))) : null;
        } else {
            object4 = cv;
            v = null;
        }
        Object object16 = v = object4;
        if (object16 != null && object16 != Boolean.FALSE) {
            Object object17 = ((DbRel)this_.dbrel).isref;
            if (object17 != null && object17 != Boolean.FALSE) {
                Object object18 = v;
                v = null;
                object3 = ((IFn)const__11.getRawRoot()).invoke(this_.db, object18);
            } else {
                Object object19 = ((DbRel)this_.dbrel).iskey;
                if (object19 != null && object19 != Boolean.FALSE) {
                    Object object20 = v;
                    v = null;
                    object3 = ((IFn)const__15.getRawRoot()).invoke(object20);
                } else {
                    Keyword keyword = const__7;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        object3 = v;
                        v = null;
                    } else {
                        object3 = null;
                    }
                }
            }
        } else {
            object3 = v;
            v = null;
        }
        Object object21 = temp__5457__auto__18249 = RT.get((Object)this_.consts, (Object)const__16);
        if (object21 != null && object21 != Boolean.FALSE) {
            Object t;
            Object object22 = temp__5457__auto__18249;
            temp__5457__auto__18249 = null;
            object2 = t = object22;
            t = null;
        } else {
            object2 = null;
        }
        Object object23 = or__5238__auto__18250 = object2;
        if (object23 != null && object23 != Boolean.FALSE) {
            object = or__5238__auto__18250;
            or__5238__auto__18250 = null;
        } else {
            Object object24 = RT.aget((Object[])((Object[])this_.bindings), (int)((int)3L));
            if (object24 != null && object24 != Boolean.FALSE) {
                Object object25 = y;
                y = null;
                object = RT.nth((Object)object25, (int)RT.uncheckedIntCast((Object)((Number)RT.aget((Object[])((Object[])this_.bindings), (int)((int)3L)))));
            } else {
                object = const__17;
            }
        }
        datalog$fn__18233$bind__18245 this_ = null;
        return iFn.invoke(object6, object5, object3, object);
    }
}

