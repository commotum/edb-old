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
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
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
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.valcache$eviction_loop$evict1$reify__9768;
import datomic.valcache$eviction_loop$evict1__9765$fn__9766;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.PriorityQueue;

public final class valcache$eviction_loop$evict1__9765
extends AFunction {
    Object opts;
    Object file_window;
    Object threshold;
    Object path;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__3 = RT.classForName((String)"java.lang.String");
    public static final Var const__4 = RT.var((String)"datomic.valcache", (String)"dirname");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"comparator");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"atom");
    public static final Object const__7 = 0L;
    public static final AFn const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 339, RT.keyword(null, (String)"column"), 21});
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"deref");
    public static final Keyword const__23 = RT.keyword(null, (String)"deleted-files");
    public static final Keyword const__24 = RT.keyword(null, (String)"deleted-bytes");
    public static final Keyword const__25 = RT.keyword(null, (String)"visited-files");
    public static final Keyword const__26 = RT.keyword(null, (String)"visited-bytes");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__32 = RT.keyword(null, (String)"atime");
    public static final Keyword const__33 = RT.keyword(null, (String)"length");
    public static final Keyword const__34 = RT.keyword(null, (String)"file");

    public valcache$eviction_loop$evict1__9765(Object object, Object object2, Object object3, Object object4) {
        this.opts = object;
        this.file_window = object2;
        this.threshold = object3;
        this.path = object4;
    }

    public Object invoke(Object dir) {
        IPersistentMap iPersistentMap;
        block5: {
            Number threshold = Numbers.divide((Object)this.threshold, (long)4096L);
            Object object = dir;
            dir = null;
            Path path2 = FileSystems.getDefault().getPath((String)this.path, (String[])((IFn)const__2.getRawRoot()).invoke(const__3, (Object)Tuple.create((Object)((IFn)const__4.getRawRoot()).invoke(object))));
            PriorityQueue pq = new PriorityQueue(RT.intCast((Object)((Number)this.file_window)), (Comparator)((IFn)const__5.getRawRoot()).invoke((Object)new valcache$eviction_loop$evict1__9765$fn__9766()));
            Object size = ((IFn)const__6.getRawRoot()).invoke(const__7);
            Object visited = ((IFn)const__6.getRawRoot()).invoke(const__7);
            Path path3 = path2;
            path2 = null;
            Files.walkFileTree(path3, (FileVisitor)((IObj)new valcache$eviction_loop$evict1$reify__9768(null, visited, size, this.opts, pq, this.file_window)).withMeta((IPersistentMap)const__12));
            boolean and__5236__auto__9772 = Numbers.gt((Object)((IFn)const__14.getRawRoot()).invoke(size), (Object)threshold);
            if (and__5236__auto__9772 ? Numbers.isPos((long)RT.count(pq)) : and__5236__auto__9772) {
                PriorityQueue priorityQueue = pq;
                pq = null;
                Object[] fa = priorityQueue.toArray();
                Number number = threshold;
                threshold = null;
                double target2 = Numbers.minus((Object)((IFn)const__14.getRawRoot()).invoke(size), (double)Numbers.multiply((double)0.9, (Object)number));
                long i = Numbers.dec((long)RT.count((Object)fa));
                long deleted = 0L;
                long files2 = 0L;
                while (true) {
                    Object file;
                    Object object2;
                    boolean or__5238__auto__9773;
                    if ((or__5238__auto__9773 = Numbers.isNeg((long)i)) ? or__5238__auto__9773 : Numbers.gte((long)deleted, (double)target2)) {
                        IPersistentMap result2;
                        iPersistentMap = result2 = RT.mapUniqueKeys((Object[])new Object[]{const__23, Numbers.num((long)files2), const__24, Numbers.num((long)deleted), const__25, ((IFn)const__14.getRawRoot()).invoke(visited), const__26, ((IFn)const__14.getRawRoot()).invoke(size)});
                        result2 = null;
                        break block5;
                    }
                    Object map__9770 = RT.aget((Object[])fa, (int)RT.intCast((long)i));
                    Object object3 = ((IFn)const__29.getRawRoot()).invoke(map__9770);
                    if (object3 != null && object3 != Boolean.FALSE) {
                        Object object4 = map__9770;
                        map__9770 = null;
                        object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__30.getRawRoot()).invoke(object4)));
                    } else {
                        object2 = map__9770;
                        map__9770 = null;
                    }
                    Object map__97702 = object2;
                    RT.get((Object)map__97702, (Object)const__32);
                    Object length = RT.get((Object)map__97702, (Object)const__33);
                    Object object5 = map__97702;
                    map__97702 = null;
                    Object object6 = file = RT.get((Object)object5, (Object)const__34);
                    file = null;
                    Files.deleteIfExists((Path)object6);
                    Object object7 = length;
                    length = null;
                    files2 = Numbers.inc((long)files2);
                    deleted = Numbers.add((long)deleted, (long)RT.longCast((Object)((Number)object7)));
                    i = Numbers.dec((long)i);
                }
            }
            iPersistentMap = null;
        }
        return iPersistentMap;
    }
}

