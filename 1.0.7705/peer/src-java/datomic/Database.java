/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.RT;
import datomic.Attribute;
import datomic.Datom;
import datomic.Entity;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface Database {
    public static final Object EAVT = RT.keyword(null, (String)"eavt");
    public static final Object AEVT = RT.keyword(null, (String)"aevt");
    public static final Object AVET = RT.keyword(null, (String)"avet");
    public static final Object VAET = RT.keyword(null, (String)"vaet");

    public String id();

    public long basisT();

    public long nextT();

    public Long asOfT();

    public Long sinceT();

    public boolean isHistory();

    public Map with(List var1);

    public Map with(List var1, Object var2);

    public Database asOf(Object var1);

    public Database since(Object var1);

    public Database history();

    public Database filter(Predicate<Datom> var1);

    public Database filter(Object var1);

    public boolean isFiltered();

    public Entity entity(Object var1);

    public Attribute attribute(Object var1);

    public Object ident(Object var1);

    public Object entid(Object var1);

    public Object entidAt(Object var1, Object var2);

    public Object invoke(Object var1, Object ... var2);

    public Iterable<Datom> datoms(Object var1, Object ... var2);

    public Iterable<Datom> seekDatoms(Object var1, Object ... var2);

    public Iterable<Datom> rseekDatoms(Object var1, Object ... var2);

    public Iterable<Datom> indexRange(Object var1, Object var2, Object var3);

    public Map pull(Object var1, Object var2);

    public Object pull(Object var1, Object var2, Object var3);

    public Stream<Object> indexPull(Object var1);

    public List<Map> pullMany(Object var1, List var2);

    public Object pullMany(Object var1, List var2, Object var3);

    public Map dbStats();

    public static interface Predicate<T> {
        public boolean apply(Database var1, T var2);
    }
}
