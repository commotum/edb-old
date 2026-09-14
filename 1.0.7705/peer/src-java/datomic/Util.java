package datomic;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utilities for constructing immutable Java data structures, reading EDN,
 * and adapting immutable iterables to streams.
 */
// ATOMIC-NOTE [observed/disposition]: Java collection/EDN/stream conveniences
// adapt the host to the Clojure API; they are not database structures. The
// null list/map cases are mutable and other wrappers are shallow. Native
// model/query values and EDN adapters specify their own ownership and shapes;
// neither Java wrapper mutability nor Spliterator flags define DB semantics.
public final class Util {
    private static final Var REQUIRE = RT.var((String)"clojure.core", (String)"require");
    private static final Var NAME;
    private static final Var NAMESPACE;
    private static final Var READ_STRING;
    private static final Var READ_ALL;

    private Util() {
    }

    /**
     * Returns the unqualified name of a keyword or symbol.
     *
     * @param k keyword or symbol
     * @return the unqualified name
     */
    public static String name(Object k) {
        return (String)NAME.invoke(k);
    }

    /**
     * Returns the namespace of a keyword or symbol.
     *
     * @param k keyword or symbol
     * @return namespace, or {@code null} for an unqualified value
     */
    public static String namespace(Object k) {
        return (String)NAMESPACE.invoke(k);
    }

    /**
     * Creates an immutable list containing {@code items} in order.
     *
     * @param items list elements
     * @return an immutable list containing {@code items}
     */
    public static List list(Object ... items) {
        if (items == null) {
            return new ArrayList();
        }
        List list = new ArrayList(items.length);
        for (int i = 0; i < items.length; ++i) {
            list.add(items[i]);
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Creates an immutable map from alternating keys and values.
     *
     * @param keyvals key, value, key, value, and so on
     * @return an immutable map containing the supplied entries
     * @throws IllegalArgumentException when an odd number of values is supplied
     */
    public static Map map(Object ... keyvals) {
        if (keyvals == null) {
            return new HashMap();
        }
        if (keyvals.length % 2 != 0) {
            throw new IllegalArgumentException("Map must have an even number of elements");
        }
        Map m = new HashMap(keyvals.length / 2);
        for (int i = 0; i < keyvals.length; i += 2) {
            m.put(keyvals[i], keyvals[i + 1]);
        }
        return Collections.unmodifiableMap(m);
    }

    /**
     * Reads and returns one EDN value.
     *
     * @param source EDN source text
     * @return the parsed EDN value
     */
    public static Object read(String source) {
        return READ_STRING.invoke((Object)source);
    }

    /**
     * Reads all EDN values from a reader and closes it.
     *
     * @param reader EDN input
     * @return parsed values in source order
     */
    public static List readAll(Reader reader) {
        return (List)READ_ALL.invoke((Object)reader);
    }

    /**
     * Creates a sequential stream over an immutable iterable.
     *
     * @param iterable values to stream
     * @return an empty stream when {@code iterable} is {@code null}
     */
    public static Stream streamOn(Iterable iterable) {
        if (iterable == null) {
            return Stream.empty();
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterable.iterator(), 1040), false);
    }

    static {
        REQUIRE.invoke((Object)Symbol.intern((String)"clojure.edn"));
        REQUIRE.invoke((Object)Symbol.intern((String)"datomic.io"));
        REQUIRE.invoke((Object)Symbol.intern((String)"datomic.db"));
        NAME = RT.var((String)"clojure.core", (String)"name");
        NAMESPACE = RT.var((String)"clojure.core", (String)"namespace");
        READ_STRING = RT.var((String)"clojure.edn", (String)"read-string");
        READ_ALL = RT.var((String)"datomic.io", (String)"read-all");
    }
}
