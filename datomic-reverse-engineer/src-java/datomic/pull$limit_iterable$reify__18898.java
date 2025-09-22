/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import datomic.pull$limit_iterable$reify$reify__18899;
import java.util.Iterator;

public final class pull$limit_iterable$reify__18898
implements Iterable,
IObj {
    final IPersistentMap __meta;
    Object iterable;
    Object limit;
    public static final Object const__2 = 0L;
    public static final AFn const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 34, RT.keyword(null, (String)"column"), 11});

    public pull$limit_iterable$reify__18898(IPersistentMap iPersistentMap, Object object, Object object2) {
        this.__meta = iPersistentMap;
        this.iterable = object;
        this.limit = object2;
    }

    public pull$limit_iterable$reify__18898(Object object, Object object2) {
        this(null, object, object2);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new pull$limit_iterable$reify__18898(iPersistentMap, this.iterable, this.limit);
    }

    public Iterator iterator() {
        Iterator iter2 = ((Iterable)this.iterable).iterator();
        long[] i = Numbers.long_array((int)RT.intCast((long)1L), (Object)const__2);
        Iterator iterator2 = iter2;
        iter2 = null;
        long[] lArray = i;
        i = null;
        return (Iterator)((IObj)new pull$limit_iterable$reify$reify__18899(null, iterator2, lArray, this.limit)).withMeta((IPersistentMap)const__7);
    }
}

