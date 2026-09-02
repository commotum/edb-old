package datomic.impl.db;

/**
 * Primitive-access view of an internal datom. It exposes entity, attribute,
 * transaction, partition, and assertion state together with typed value
 * accessors so index operations can avoid unnecessary boxing.
 */
public interface IDatum {
    public boolean isAssertion();

    public long getE();

    public int getA();

    public long getT();

    public long getTx();

    public int getP();

    public Object getV();

    public long getLongV();

    public double getDoubleV();

    public int getIntV();

    public float getFloatV();

    public boolean getBooleanV();
}
