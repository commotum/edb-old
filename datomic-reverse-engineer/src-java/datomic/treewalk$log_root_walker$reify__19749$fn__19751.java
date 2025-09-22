/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.treewalk$log_root_walker$reify__19749$fn$reify__19753;
import datomic.treewalk.Node;

public final class treewalk$log_root_walker$reify__19749$fn__19751
extends AFunction {
    Object lookup;
    Object allow_missing_QMARK_;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"uuid");
    public static final Var const__4 = RT.var((String)"datomic.treewalk", (String)"lookup-val");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final AFn const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 148, RT.keyword(null, (String)"column"), 32});

    public treewalk$log_root_walker$reify__19749$fn__19751(Object object, Object object2) {
        this.lookup = object;
        this.allow_missing_QMARK_ = object2;
    }

    public Object invoke(Object p__19750) {
        Node node;
        Object temp__5457__auto__19756;
        Object map__19752;
        Object object;
        Object object2 = p__19750;
        p__19750 = null;
        Object map__197522 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__197522);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__197522;
            map__197522 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__197522;
            map__197522 = null;
        }
        Object object5 = map__19752 = object;
        map__19752 = null;
        Object uuid = RT.get((Object)object5, (Object)const__3);
        Object object6 = temp__5457__auto__19756 = ((IFn)const__4.getRawRoot()).invoke(this.lookup, ((IFn)const__5.getRawRoot()).invoke(uuid), this.allow_missing_QMARK_);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = temp__5457__auto__19756;
            temp__5457__auto__19756 = null;
            Object v = object7;
            uuid = null;
            v = null;
            node = new Node(((IFn)const__5.getRawRoot()).invoke(uuid), ((IObj)new treewalk$log_root_walker$reify__19749$fn$reify__19753(null, v)).withMeta((IPersistentMap)const__10));
        } else {
            node = null;
        }
        return node;
    }
}

