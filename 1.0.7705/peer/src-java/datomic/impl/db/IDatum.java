package datomic.impl.db;

/**
 * Primitive-access view of an internal datom. It exposes entity, attribute,
 * transaction, partition, and assertion state together with typed value
 * accessors so index operations can avoid unnecessary boxing.
 */
// ATOMIC-NOTE BEGIN foundation-idatum-counterpart
// Baseline-identical to transactor/src-java/datomic/impl/db/IDatum.java at
// cd7192e63d883a4a34aa7de4d5bcd17e6edb692d; see its foundation-idatum note for
// primitive access and the getT/getTx distinction implemented by db/Datum.
// ATOMIC-NOTE END foundation-idatum-counterpart
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
