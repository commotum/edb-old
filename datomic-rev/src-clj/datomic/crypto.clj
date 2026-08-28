(do
  (clojure.core/in-ns 'datomic.crypto)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.codec :as 'codec]
        ['clojure.java.io :as 'jio]
        ['datomic.io :as 'dio])
      (clojure.core/import 'java.util.Arrays)
      (clojure.core/import 'javax.crypto.KeyGenerator)
      (clojure.core/import 'javax.crypto.SecretKey)
      (clojure.core/import 'javax.crypto.SecretKeyFactory)
      (clojure.core/import 'javax.crypto.Cipher)
      (clojure.core/import 'javax.crypto.Mac)
      (clojure.core/import 'javax.crypto.spec.SecretKeySpec)
      (clojure.core/import 'javax.crypto.spec.IvParameterSpec)
      (clojure.core/import 'java.security.spec.X509EncodedKeySpec)
      (clojure.core/import 'java.nio.ByteBuffer)
      (clojure.core/import 'java.security.Key)
      (clojure.core/import 'java.security.KeyFactory)
      (clojure.core/import 'java.security.KeyStore)
      (clojure.core/import 'java.security.PublicKey)
      (clojure.core/import 'java.security.PrivateKey)
      (clojure.core/import 'java.security.SecureRandom)
      (clojure.core/import 'java.security.Signature)))
  (when-not (.equals 'datomic.crypto 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.crypto))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.codec :as 'codec]
          ['clojure.java.io :as 'jio]
          ['datomic.io :as 'dio])
        (clojure.core/import 'java.util.Arrays)
        (clojure.core/import 'javax.crypto.KeyGenerator)
        (clojure.core/import 'javax.crypto.SecretKey)
        (clojure.core/import 'javax.crypto.SecretKeyFactory)
        (clojure.core/import 'javax.crypto.Cipher)
        (clojure.core/import 'javax.crypto.Mac)
        (clojure.core/import 'javax.crypto.spec.SecretKeySpec)
        (clojure.core/import 'javax.crypto.spec.IvParameterSpec)
        (clojure.core/import 'java.security.spec.X509EncodedKeySpec)
        (clojure.core/import 'java.nio.ByteBuffer)
        (clojure.core/import 'java.security.Key)
        (clojure.core/import 'java.security.KeyFactory)
        (clojure.core/import 'java.security.KeyStore)
        (clojure.core/import 'java.security.PublicKey)
        (clojure.core/import 'java.security.PrivateKey)
        (clojure.core/import 'java.security.SecureRandom)
        (clojure.core/import 'java.security.Signature))))
  (set! *warn-on-reflection* true)
  (defn random-string
    ([entropy]
      (let [sr (java.security.SecureRandom.) b (byte-array (quot (+ 7 entropy) 8))]
        (.nextBytes ^java.security.SecureRandom sr ^bytes b)
        (codec/bytes->string (codec/encode-64 b)))))
  (defn cipher ([name] (Cipher/getInstance ^java.lang.String name)))
  (reset-meta!
    #'cipher
    (assoc
      {:tag javax.crypto.Cipher, :arglists (clojure.core/list ['name]), :column 1}
      :name
      'cipher
      :ns
      *ns*))
  (defn genkey
    ([cipher_name]
      (let [keygen (KeyGenerator/getInstance ^java.lang.String cipher_name)]
        (.init ^javax.crypto.KeyGenerator keygen (int 128))
        (.getEncoded (.generateKey ^javax.crypto.KeyGenerator keygen)))))
  (defn mac
    ([plaintext secret]
      (codec/bytes->string
        (codec/encode-64
          (.doFinal
            (doto
              (Mac/getInstance "HmacSHA256")
              (.init
                (javax.crypto.spec.SecretKeySpec.
                  (.getBytes ^java.lang.String secret)
                  "HmacSHA256"))
              (.reset))
            (codec/string->bytes plaintext))))))
  (defn encrypt-pk
    ([plaintext key]
      (let [c (cipher (.getAlgorithm ^java.security.Key key))]
        (.init ^javax.crypto.Cipher c (int Cipher/ENCRYPT_MODE) ^java.security.Key key)
        (.doFinal ^javax.crypto.Cipher c (codec/string->bytes plaintext)))))
  (defn spec->public-key
    ([algo b64encoded]
      (let [kf (KeyFactory/getInstance ^java.lang.String algo)
            encoded (codec/decode-64 (codec/string->bytes b64encoded))]
        (.generatePublic
          ^java.security.KeyFactory kf
          (java.security.spec.X509EncodedKeySpec. ^bytes encoded)))))
  (defn decrypt-pk
    ([ciphertext key]
      (let [c (cipher (.getAlgorithm ^java.security.Key key))]
        (.init ^javax.crypto.Cipher c (int Cipher/DECRYPT_MODE) ^java.security.Key key)
        (codec/bytes->string (.doFinal ^javax.crypto.Cipher c ^bytes ciphertext)))))
  (defn encrypt
    ([plaintext cipher_name rawkey]
      (let [c (cipher cipher_name)]
        (.init
          ^javax.crypto.Cipher c
          (int Cipher/ENCRYPT_MODE)
          (javax.crypto.spec.SecretKeySpec. ^bytes rawkey ^java.lang.String cipher_name))
        (.doFinal ^javax.crypto.Cipher c (codec/string->bytes plaintext)))))
  (defn decrypt
    ([ciphertext cipher_name rawkey]
      (let [c (cipher cipher_name)]
        (.init
          ^javax.crypto.Cipher c
          (int Cipher/DECRYPT_MODE)
          (javax.crypto.spec.SecretKeySpec. ^bytes rawkey ^java.lang.String cipher_name))
        (codec/bytes->string (.doFinal ^javax.crypto.Cipher c ^bytes ciphertext)))))
  (def random (java.security.SecureRandom.))
  (reset-meta!
    #'random
    (assoc {:tag java.security.SecureRandom, :column 1} :name 'random :ns *ns*))
  (defn random-bytes ([n] (let [dest (byte-array n)] (.nextBytes random ^bytes dest) dest)))
  (defn read-n
    ([bb n] (let [dest (byte-array n)] (.get ^java.nio.ByteBuffer bb ^bytes dest) dest)))
  (defn xor-arrays
    ([b1 b2]
      (when-not (= (count b1) (count b2))
        (throw
          (java.lang.AssertionError.
            (str
              "Assert failed: "
              (pr-str
                (clojure.core/list
                  '=
                  (clojure.core/list 'count 'b1)
                  (clojure.core/list 'count 'b2)))))))
      (let [result (byte-array (java.lang.Integer/valueOf (int (count b1))))]
        (loop [n 0]
          (when (< n (count b1))
            (aset-byte
              result
              (long n)
              (long
                (bit-xor
                  (long (java.lang.Byte/valueOf (byte (aget ^bytes b1 (int n)))))
                  (long (java.lang.Byte/valueOf (byte (aget ^bytes b2 (int n))))))))
            (recur (inc n))))
        result)))
  (defn split-array
    ([b1]
      (let [b2 (random-bytes (java.lang.Integer/valueOf (int (count b1))))]
        [b2 (xor-arrays b1 b2)])))
  (defn split-key
    ([k]
      (map
        (fn fn__23356
          ([b]
            {:fragment (codec/bytes->string (codec/encode-64 b)),
             :algorithm (.getAlgorithm ^java.security.Key k)}))
        (split-array (.getEncoded ^java.security.Key k)))))
  (defn ser-key
    ([k]
      {:key (codec/bytes->string (codec/encode-64 (.getEncoded ^java.security.Key k))),
       :alg (.getAlgorithm ^java.security.Key k)}))
  (defn deser-key
    ([p__23360]
      (let [map__23361 p__23360
            map__23361 (if (seq? map__23361)
                         (clojure.lang.PersistentHashMap/create (seq map__23361))
                         map__23361)
            key (get map__23361 :key)
            alg (get map__23361 :alg)
            data (codec/decode-64 (codec/string->bytes key))]
        (javax.crypto.spec.SecretKeySpec.
          ^bytes data
          (int 0)
          (int (count data))
          ^java.lang.String alg))))
  (defn combine-fragments
    ([f1 f2]
      (let [b1 (codec/decode-64 (codec/string->bytes (:fragment f1)))
            b2 (codec/decode-64 (codec/string->bytes (:fragment f2)))]
        (javax.crypto.spec.SecretKeySpec. (xor-arrays b1 b2) (:algorithm f2)))))
  (def hmac-length
   (memoize
     (fn fn__23364
       ([algorithm]
         (java.lang.Integer/valueOf
           (int (.getMacLength (Mac/getInstance ^java.lang.String algorithm))))))))
  (defn calc-hmac
    ([bbuf alg k]
      (let [mac (let [G__23366 (Mac/getInstance ^java.lang.String alg)]
                  (.init ^javax.crypto.Mac G__23366 ^java.security.Key k)
                  G__23366)
            result (.duplicate ^java.nio.ByteBuffer bbuf)]
        (.update ^javax.crypto.Mac mac ^java.nio.ByteBuffer result)
        (.doFinal ^javax.crypto.Mac mac))))
  (defn append-hmac
    ([bbuf alg k]
      (let [result (.duplicate ^java.nio.ByteBuffer bbuf)]
        (.position ^java.nio.ByteBuffer result (int (.limit ^java.nio.Buffer result)))
        (.limit
          ^java.nio.ByteBuffer result
          (int (+ (.limit ^java.nio.Buffer result) (hmac-length alg))))
        (.put ^java.nio.ByteBuffer result ^bytes (calc-hmac bbuf alg k))
        (.flip ^java.nio.ByteBuffer result))))
  (defn validate-hmac
    ([bbuf alg k]
      (let [hmac_offset (- (.limit ^java.nio.Buffer bbuf) (hmac-length alg))
            _ (when-not (dio/valid-buf-limit? bbuf hmac_offset)
                (throw
                  (ex-info
                    "HMAC integrity check failed"
                    #:db{:error :datomic.crypto/hmac-missing}))
                nil)
            result (.limit (.duplicate ^java.nio.ByteBuffer bbuf) (int hmac_offset))
            buf_hmac (byte-array (hmac-length alg))
            input (.position (.duplicate ^java.nio.ByteBuffer bbuf) (int hmac_offset))]
        (.get ^java.nio.ByteBuffer input ^bytes buf_hmac)
        (if (Arrays/equals ^bytes buf_hmac ^bytes (calc-hmac result alg k))
          result
          (do
            (throw
              (ex-info
                "HMAC integrity check failed"
                #:db{:error :datomic.crypto/validate-hmac-failed}))
            nil)))))
  (defn crypt-bbuf
    ([input k op extra]
      (let [ciph (cipher (str (.getAlgorithm ^java.security.Key k) "/CBC/PKCS5Padding"))
            iv_size (.getBlockSize ^javax.crypto.Cipher ciph)
            src (.duplicate ^java.nio.ByteBuffer input)
            ivbytes (let [G__23370 op]
                      (case
                        G__23370
                        :encrypt
                        (random-bytes (java.lang.Integer/valueOf (int iv_size)))
                        :decrypt
                        (read-n src (java.lang.Integer/valueOf (int iv_size)))))
            iv (javax.crypto.spec.IvParameterSpec. ^bytes ivbytes)]
        (.init
          ^javax.crypto.Cipher ciph
          (int
            (let [G__23371 op]
              (case G__23371 :encrypt Cipher/ENCRYPT_MODE :decrypt Cipher/DECRYPT_MODE)))
          ^java.security.Key k
          ^java.security.spec.AlgorithmParameterSpec iv)
        (let [n (.getOutputSize ^javax.crypto.Cipher ciph (int (.remaining ^java.nio.Buffer src)))
              dest (ByteBuffer/allocate (int (+ (+ iv_size n) extra)))]
          (when (= :encrypt op) (.put ^java.nio.ByteBuffer dest ^bytes ivbytes))
          (java.lang.Integer/valueOf
            (int
              (.doFinal
                ^javax.crypto.Cipher ciph
                ^java.nio.ByteBuffer src
                ^java.nio.ByteBuffer dest)))
          (.flip ^java.nio.ByteBuffer dest))))
    ([input k op] (crypt-bbuf input k op 0)))
  (defn sign
    ([algo priv arr]
      (let [signer (Signature/getInstance ^java.lang.String algo)]
        (.initSign ^java.security.Signature signer ^java.security.PrivateKey priv)
        (.update ^java.security.Signature signer ^bytes arr (int 0) (int (count arr)))
        (.sign ^java.security.Signature signer))))
  (defn verify
    ([algo pub arr sig]
      (let [verifier (Signature/getInstance ^java.lang.String algo)]
        (.initVerify ^java.security.Signature verifier ^java.security.PublicKey pub)
        (.update ^java.security.Signature verifier ^bytes arr (int 0) (int (count arr)))
        (.verify ^java.security.Signature verifier ^bytes sig))))
  (defn keystore
    ([f password type]
      (let [is (jio/input-stream f)]
        (try
          (let [G__23375 (KeyStore/getInstance ^java.lang.String type)]
            (.load ^java.security.KeyStore G__23375 ^java.io.InputStream is ^chars password)
            G__23375)
          (finally (do (.close ^java.io.InputStream is) nil)))))
    ([f password] (keystore f password (KeyStore/getDefaultType))))
  (defn load-private-key
    ([ks alias password]
      (.getKey ^java.security.KeyStore ks ^java.lang.String alias ^chars password)))
  (defn load-public-key
    ([ks alias]
      (.getPublicKey (.getCertificate ^java.security.KeyStore ks ^java.lang.String alias)))))
