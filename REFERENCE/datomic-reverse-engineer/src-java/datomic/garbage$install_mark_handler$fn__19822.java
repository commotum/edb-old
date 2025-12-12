/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class garbage$install_mark_handler$fn__19822
extends AFunction {
    Object olookups;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"cluster");
    public static final Keyword const__4 = RT.keyword(null, (String)"garbage");
    public static final Var const__5 = RT.var((String)"datomic.garbage", (String)"mark-garbage");

    public garbage$install_mark_handler$fn__19822(Object object) {
        this.olookups = object;
    }

    public Object invoke(Object p__19821) {
        Object object;
        Object object2 = p__19821;
        p__19821 = null;
        Object map__19823 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__19823);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__19823;
            map__19823 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__19823;
            map__19823 = null;
        }
        Object map__198232 = object;
        Object cluster2 = RT.get((Object)map__198232, (Object)const__3);
        Object object5 = map__198232;
        map__198232 = null;
        Object garbage2 = RT.get((Object)object5, (Object)const__4);
        Object object6 = cluster2;
        Object object7 = cluster2;
        cluster2 = null;
        Object object8 = garbage2;
        garbage2 = null;
        garbage$install_mark_handler$fn__19822 this_ = null;
        return ((IFn)const__5.getRawRoot()).invoke(object6, RT.get((Object)this_.olookups, (Object)object7), object8);
    }
}

