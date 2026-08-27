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
import java.io.Closeable;

public final class fulltext_index$update_fulltext$fn__12377
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__6 = RT.keyword(null, (String)"directory");
    public static final Keyword const__7 = RT.keyword(null, (String)"writer");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"deref");

    public Object invoke(Object pft, Object p__12376) {
        Object writer2;
        Object object;
        Object object2 = p__12376;
        p__12376 = null;
        Object vec__12378 = object2;
        Object k = RT.nth((Object)vec__12378, (int)RT.intCast((long)0L), null);
        Object object3 = vec__12378;
        vec__12378 = null;
        Object map__12381 = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        Object object4 = ((IFn)const__3.getRawRoot()).invoke(map__12381);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__12381;
            map__12381 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__4.getRawRoot()).invoke(object5)));
        } else {
            object = map__12381;
            map__12381 = null;
        }
        Object map__123812 = object;
        Object directory = RT.get((Object)map__123812, (Object)const__6);
        Object object6 = map__123812;
        map__123812 = null;
        Object object7 = writer2 = RT.get((Object)object6, (Object)const__7);
        writer2 = null;
        ((Closeable)object7).close();
        Object object8 = pft;
        pft = null;
        Object object9 = k;
        k = null;
        Object object10 = directory;
        directory = null;
        fulltext_index$update_fulltext$fn__12377 this_ = null;
        return ((IFn)const__8.getRawRoot()).invoke(object8, object9, ((IFn)const__9.getRawRoot()).invoke(object10));
    }
}

