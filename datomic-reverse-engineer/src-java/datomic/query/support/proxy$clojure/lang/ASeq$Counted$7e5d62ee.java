/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.ASeq
 *  clojure.lang.Counted
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentCollection
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IProxy
 *  clojure.lang.ISeq
 *  clojure.lang.Obj
 *  clojure.lang.RT
 */
package datomic.query.support.proxy$clojure.lang;

import clojure.lang.ASeq;
import clojure.lang.Counted;
import clojure.lang.IFn;
import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentMap;
import clojure.lang.IProxy;
import clojure.lang.ISeq;
import clojure.lang.Obj;
import clojure.lang.RT;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ASeq$Counted$7e5d62ee
extends ASeq
implements IProxy,
Counted {
    private volatile IPersistentMap __clojureFnMap;

    public ASeq$Counted$7e5d62ee(IPersistentMap iPersistentMap) {
        ASeq$Counted$7e5d62ee aSeq$Counted$7e5d62ee = this;
        ASeq$Counted$7e5d62ee aSeq$Counted$7e5d62ee2 = aSeq$Counted$7e5d62ee;
        super(iPersistentMap);
    }

    public ASeq$Counted$7e5d62ee() {
        ASeq$Counted$7e5d62ee aSeq$Counted$7e5d62ee = this;
        ASeq$Counted$7e5d62ee aSeq$Counted$7e5d62ee2 = aSeq$Counted$7e5d62ee;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) {
        ASeq$Counted$7e5d62ee aSeq$Counted$7e5d62ee = this;
        ObjectOutputStream objectOutputStream2 = objectOutputStream;
        throw new NotSerializableException("datomic.query.support.proxy$clojure.lang.ASeq$Counted$7e5d62ee");
    }

    private void readObject(ObjectInputStream objectInputStream) {
        ASeq$Counted$7e5d62ee aSeq$Counted$7e5d62ee = this;
        ObjectInputStream objectInputStream2 = objectInputStream;
        throw new NotSerializableException("datomic.query.support.proxy$clojure.lang.ASeq$Counted$7e5d62ee");
    }

    public void __initClojureFnMappings(IPersistentMap iPersistentMap) {
        this.__clojureFnMap = iPersistentMap;
    }

    public void __updateClojureFnMappings(IPersistentMap iPersistentMap) {
        this.__clojureFnMap = (IPersistentMap)((IPersistentCollection)this.__clojureFnMap).cons((Object)iPersistentMap);
    }

    public IPersistentMap __getClojureFnMappings() {
        return this.__clojureFnMap;
    }

    public boolean contains(Object object) {
        Object object2 = RT.get((Object)this.__clojureFnMap, (Object)"contains");
        return object2 != null ? ((Boolean)((IFn)object2).invoke((Object)this, object)).booleanValue() : super.contains(object);
    }

    public boolean addAll(Collection collection) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"addAll");
        return object != null ? ((Boolean)((IFn)object).invoke((Object)this, (Object)collection)).booleanValue() : super.addAll(collection);
    }

    public boolean retainAll(Collection collection) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"retainAll");
        return object != null ? ((Boolean)((IFn)object).invoke((Object)this, (Object)collection)).booleanValue() : super.retainAll(collection);
    }

    public int count() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"count");
        return object != null ? ((Number)((IFn)object).invoke((Object)this)).intValue() : super.count();
    }

    public Iterator iterator() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"iterator");
        return object != null ? (Iterator)((IFn)object).invoke((Object)this) : super.iterator();
    }

    public int hasheq() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"hasheq");
        return object != null ? ((Number)((IFn)object).invoke((Object)this)).intValue() : super.hasheq();
    }

    public boolean equals(Object object) {
        Object object2 = RT.get((Object)this.__clojureFnMap, (Object)"equals");
        return object2 != null ? ((Boolean)((IFn)object2).invoke((Object)this, object)).booleanValue() : super.equals(object);
    }

    public Spliterator spliterator() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"spliterator");
        return object != null ? (Spliterator)((IFn)object).invoke((Object)this) : super.spliterator();
    }

    public boolean removeAll(Collection collection) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"removeAll");
        return object != null ? ((Boolean)((IFn)object).invoke((Object)this, (Object)collection)).booleanValue() : super.removeAll(collection);
    }

    public Object get(int n) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"get");
        return object != null ? ((IFn)object).invoke((Object)this, (Object)n) : super.get(n);
    }

    public int indexOf(Object object) {
        Object object2 = RT.get((Object)this.__clojureFnMap, (Object)"indexOf");
        return object2 != null ? ((Number)((IFn)object2).invoke((Object)this, object)).intValue() : super.indexOf(object);
    }

    public ISeq cons(Object object) {
        Object object2 = RT.get((Object)this.__clojureFnMap, (Object)"cons");
        return object2 != null ? (ISeq)((IFn)object2).invoke((Object)this, object) : super.cons(object);
    }

    public int size() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"size");
        return object != null ? ((Number)((IFn)object).invoke((Object)this)).intValue() : super.size();
    }

    public void sort(Comparator comparator) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"sort");
        if (object != null) {
            ((IFn)object).invoke((Object)this, (Object)comparator);
        } else {
            super.sort(comparator);
        }
    }

    public Object clone() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"clone");
        return object != null ? ((IFn)object).invoke((Object)this) : super.clone();
    }

    public ISeq more() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"more");
        return object != null ? (ISeq)((IFn)object).invoke((Object)this) : super.more();
    }

    public IPersistentCollection empty() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"empty");
        return object != null ? (IPersistentCollection)((IFn)object).invoke((Object)this) : super.empty();
    }

    public boolean add(Object object) {
        Object object2 = RT.get((Object)this.__clojureFnMap, (Object)"add");
        return object2 != null ? ((Boolean)((IFn)object2).invoke((Object)this, object)).booleanValue() : super.add(object);
    }

    public boolean isEmpty() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"isEmpty");
        return object != null ? ((Boolean)((IFn)object).invoke((Object)this)).booleanValue() : super.isEmpty();
    }

    public void forEach(Consumer consumer) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"forEach");
        if (object != null) {
            ((IFn)object).invoke((Object)this, (Object)consumer);
        } else {
            super.forEach(consumer);
        }
    }

    public void add(int n, Object object) {
        Object object2 = RT.get((Object)this.__clojureFnMap, (Object)"add");
        if (object2 != null) {
            ((IFn)object2).invoke((Object)this, (Object)n, object);
        } else {
            super.add(n, object);
        }
    }

    public Object[] toArray() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"toArray");
        return object != null ? (Object[])((IFn)object).invoke((Object)this) : super.toArray();
    }

    public ListIterator listIterator() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"listIterator");
        return object != null ? (ListIterator)((IFn)object).invoke((Object)this) : super.listIterator();
    }

    public int lastIndexOf(Object object) {
        Object object2 = RT.get((Object)this.__clojureFnMap, (Object)"lastIndexOf");
        return object2 != null ? ((Number)((IFn)object2).invoke((Object)this, object)).intValue() : super.lastIndexOf(object);
    }

    public Object remove(int n) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"remove");
        return object != null ? ((IFn)object).invoke((Object)this, (Object)n) : super.remove(n);
    }

    public Object[] toArray(IntFunction intFunction) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"toArray");
        return object != null ? (Object[])((IFn)object).invoke((Object)this, (Object)intFunction) : super.toArray(intFunction);
    }

    public ListIterator listIterator(int n) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"listIterator");
        return object != null ? (ListIterator)((IFn)object).invoke((Object)this, (Object)n) : super.listIterator(n);
    }

    public String toString() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"toString");
        return object != null ? (String)((IFn)object).invoke((Object)this) : super.toString();
    }

    public boolean removeIf(Predicate predicate) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"removeIf");
        return object != null ? ((Boolean)((IFn)object).invoke((Object)this, (Object)predicate)).booleanValue() : super.removeIf(predicate);
    }

    public boolean containsAll(Collection collection) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"containsAll");
        return object != null ? ((Boolean)((IFn)object).invoke((Object)this, (Object)collection)).booleanValue() : super.containsAll(collection);
    }

    public Object[] toArray(Object[] objectArray) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"toArray");
        return object != null ? (Object[])((IFn)object).invoke((Object)this, (Object)objectArray) : super.toArray(objectArray);
    }

    public void clear() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"clear");
        if (object != null) {
            ((IFn)object).invoke((Object)this);
        } else {
            super.clear();
        }
    }

    public Stream stream() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"stream");
        return object != null ? (Stream)((IFn)object).invoke((Object)this) : super.stream();
    }

    public List subList(int n, int n2) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"subList");
        return object != null ? (List)((IFn)object).invoke((Object)this, (Object)n, (Object)n2) : super.subList(n, n2);
    }

    public boolean equiv(Object object) {
        Object object2 = RT.get((Object)this.__clojureFnMap, (Object)"equiv");
        return object2 != null ? ((Boolean)((IFn)object2).invoke((Object)this, object)).booleanValue() : super.equiv(object);
    }

    public boolean addAll(int n, Collection collection) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"addAll");
        return object != null ? ((Boolean)((IFn)object).invoke((Object)this, (Object)n, (Object)collection)).booleanValue() : super.addAll(n, collection);
    }

    public int hashCode() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"hashCode");
        return object != null ? ((Number)((IFn)object).invoke((Object)this)).intValue() : super.hashCode();
    }

    public Object set(int n, Object object) {
        Object object2 = RT.get((Object)this.__clojureFnMap, (Object)"set");
        return object2 != null ? ((IFn)object2).invoke((Object)this, (Object)n, object) : super.set(n, object);
    }

    public void replaceAll(UnaryOperator unaryOperator) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"replaceAll");
        if (object != null) {
            ((IFn)object).invoke((Object)this, (Object)unaryOperator);
        } else {
            super.replaceAll(unaryOperator);
        }
    }

    public Stream parallelStream() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"parallelStream");
        return object != null ? (Stream)((IFn)object).invoke((Object)this) : super.parallelStream();
    }

    public boolean remove(Object object) {
        Object object2 = RT.get((Object)this.__clojureFnMap, (Object)"remove");
        return object2 != null ? ((Boolean)((IFn)object2).invoke((Object)this, object)).booleanValue() : super.remove(object);
    }

    public Obj withMeta(IPersistentMap iPersistentMap) {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"withMeta");
        if (object == null) {
            throw new UnsupportedOperationException("withMeta");
        }
        return (Obj)((IFn)object).invoke((Object)this, (Object)iPersistentMap);
    }

    public Object first() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"first");
        if (object == null) {
            throw new UnsupportedOperationException("first");
        }
        return ((IFn)object).invoke((Object)this);
    }

    public ISeq next() {
        Object object = RT.get((Object)this.__clojureFnMap, (Object)"next");
        if (object == null) {
            throw new UnsupportedOperationException("next");
        }
        return (ISeq)((IFn)object).invoke((Object)this);
    }
}

