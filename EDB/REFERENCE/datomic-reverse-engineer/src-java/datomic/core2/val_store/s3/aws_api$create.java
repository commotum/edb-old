/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic.core2.val_store.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.util.Arrays;

public final class aws_api$create
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"bucket");
    public static final Keyword const__7 = RT.keyword(null, (String)"client");
    public static final Keyword const__8 = RT.keyword(null, (String)"prefix");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__11 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"and"), Symbol.intern(null, (String)"bucket"), Symbol.intern(null, (String)"client"), Symbol.intern(null, (String)"prefix")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final Var const__12 = RT.var((String)"datomic.core2.val-store.s3.aws-api", (String)"->ValStore");

    public static Object invokeStatic(Object p__21669) {
        Object object;
        Object and__5579__auto__21673;
        Object object2;
        Object object3 = p__21669;
        p__21669 = null;
        Object map__21670 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__21670);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__21670);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__21670;
                map__21670 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object6)));
            } else {
                Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__21670);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = map__21670;
                    map__21670 = null;
                    object2 = ((IFn)const__4.getRawRoot()).invoke(object8);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__21670;
            map__21670 = null;
        }
        Object map__216702 = object2;
        Object bucket = RT.get((Object)map__216702, (Object)const__6);
        Object client2 = RT.get((Object)map__216702, (Object)const__7);
        Object object9 = map__216702;
        map__216702 = null;
        Object prefix = RT.get((Object)object9, (Object)const__8);
        Object object10 = and__5579__auto__21673 = bucket;
        if (object10 != null && object10 != Boolean.FALSE) {
            Object and__5579__auto__21672;
            Object object11 = and__5579__auto__21672 = client2;
            if (object11 != null && object11 != Boolean.FALSE) {
                object = prefix;
            } else {
                object = and__5579__auto__21672;
                and__5579__auto__21672 = null;
            }
        } else {
            object = and__5579__auto__21673;
            and__5579__auto__21673 = null;
        }
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__9.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__10.getRawRoot()).invoke(const__11))));
        }
        Object object12 = client2;
        client2 = null;
        Object object13 = bucket;
        bucket = null;
        Object object14 = prefix;
        prefix = null;
        return ((IFn)const__12.getRawRoot()).invoke(object12, object13, object14);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aws_api$create.invokeStatic(object2);
    }
}

