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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index.DirNode;

public final class index$drop_dirnode_leaves$fn__15594
extends AFunction {
    Object dirnode;
    Object drop_QMARK_;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"update");
    public static final Keyword const__2 = RT.keyword(null, (String)"garbage");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"conj");
    public static final Keyword const__4 = RT.keyword(null, (String)"keydata");
    public static final Keyword const__5 = RT.keyword(null, (String)"segids");
    public static final Keyword const__6 = RT.keyword(null, (String)"offsets");
    public static final Keyword const__7 = RT.keyword(null, (String)"counts");

    public index$drop_dirnode_leaves$fn__15594(Object object, Object object2) {
        this.dirnode = object;
        this.drop_QMARK_ = object2;
    }

    public Object invoke(Object result2, Object n) {
        Object object;
        index$drop_dirnode_leaves$fn__15594 this_;
        Object kd = RT.nth((Object)((DirNode)this_.dirnode).keydata, (int)RT.uncheckedIntCast((Object)((Number)n)));
        Object object2 = ((IFn)this_.drop_QMARK_).invoke(kd);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = result2;
            result2 = null;
            Object object4 = n;
            n = null;
            this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, (Object)const__2, const__3.getRawRoot(), RT.nth((Object)((DirNode)this_.dirnode).segids, (int)RT.uncheckedIntCast((Object)((Number)object4))));
        } else {
            Object object5 = result2;
            result2 = null;
            Object object6 = kd;
            kd = null;
            Object object7 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object5, (Object)const__4, const__3.getRawRoot(), object6), (Object)const__5, const__3.getRawRoot(), RT.nth((Object)((DirNode)this_.dirnode).segids, (int)RT.uncheckedIntCast((Object)((Number)n)))), (Object)const__6, const__3.getRawRoot(), RT.nth((Object)((DirNode)this_.dirnode).offsets, (int)RT.uncheckedIntCast((Object)((Number)n))));
            Object object8 = n;
            n = null;
            this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object7, (Object)const__7, const__3.getRawRoot(), RT.nth((Object)((DirNode)this_.dirnode).counts, (int)RT.uncheckedIntCast((Object)((Number)object8))));
        }
        return object;
    }
}

