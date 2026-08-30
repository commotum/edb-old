(do
  (clojure.core/in-ns 'datomic.codec)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'org.apache.commons.codec.binary.Base64)))
  (when-not (.equals 'datomic.codec 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.codec))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'org.apache.commons.codec.binary.Base64))))
  (defn string->bytes ([s] (.getBytes ^java.lang.String s "UTF-8")))
  (defn bytes->string ([b] (java.lang.String. ^bytes b "UTF-8")))
  (defn encode-64 ([raw] (Base64/encodeBase64 ^bytes raw (boolean (.booleanValue false)))))
  (defn decode-64 ([coded] (Base64/decodeBase64 ^bytes coded)))
  (defn base-64-literal ([literal] (decode-64 (string->bytes literal)))))