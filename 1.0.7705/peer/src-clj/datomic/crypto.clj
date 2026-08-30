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
  (def random-string
   (fn random_string
     ([entropy]
       (let [sr (java.security.SecureRandom.) b (byte-array (quot (+ 7 entropy) 8))]
         (.nextBytes ^java.security.SecureRandom sr ^bytes b)
         (codec/bytes->string (codec/encode-64 b))))))
  (reset-meta!
    #'random-string
    (assoc
      {:arglists (clojure.core/list (.withMeta ['entropy] {:tag 'java.lang.String})),
       :column (int 1)}
      :name
      'random-string
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.crypto" "cipher")
    {:tag javax.crypto.Cipher, :arglists (clojure.core/list ['name]), :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.crypto" "cipher")
    (fn cipher ([name] (Cipher/getInstance ^java.lang.String name))))
  (def genkey
   (fn genkey
     ([cipher_name]
       (let [keygen (KeyGenerator/getInstance ^java.lang.String cipher_name)]
         (.init ^javax.crypto.KeyGenerator keygen (int 128))
         (.getEncoded (.generateKey ^javax.crypto.KeyGenerator keygen))))))
  (reset-meta!
    #'genkey
    (assoc {:arglists (clojure.core/list ['cipher-name]), :column (int 1)} :name 'genkey :ns *ns*))
  (def mac
   (fn mac
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
             (codec/string->bytes plaintext)))))))
  (reset-meta!
    #'mac
    (assoc
      {:arglists
       (clojure.core/list
         [(.withMeta 'plaintext {:tag 'String}) (.withMeta 'secret {:tag 'String})]),
       :column (int 1)}
      :name
      'mac
      :ns
      *ns*))
  (def encrypt-pk
   (fn encrypt_pk
     ([plaintext key]
       (let [c (cipher (.getAlgorithm ^java.security.Key key))]
         (.init ^javax.crypto.Cipher c (int Cipher/ENCRYPT_MODE) ^java.security.Key key)
         (.doFinal ^javax.crypto.Cipher c (codec/string->bytes plaintext))))))
  (reset-meta!
    #'encrypt-pk
    (assoc
      {:arglists (clojure.core/list ['plaintext (.withMeta 'key {:tag 'PrivateKey})]),
       :column (int 1)}
      :name
      'encrypt-pk
      :ns
      *ns*))
  (defn spec->public-key
    ([algo b64encoded]
      (let [kf (KeyFactory/getInstance ^java.lang.String algo)
            encoded (codec/decode-64 (codec/string->bytes b64encoded))]
        (.generatePublic
          ^java.security.KeyFactory kf
          (java.security.spec.X509EncodedKeySpec. ^bytes encoded)))))
  (reset-meta!
    #'spec->public-key
    (assoc
      {:arglists (clojure.core/list ['algo 'b64encoded]), :column (int 1)}
      :name
      'spec->public-key
      :ns
      *ns*))
  (def decrypt-pk
   (fn decrypt_pk
     ([ciphertext key]
       (let [c (cipher (.getAlgorithm ^java.security.Key key))]
         (.init ^javax.crypto.Cipher c (int Cipher/DECRYPT_MODE) ^java.security.Key key)
         (codec/bytes->string (.doFinal ^javax.crypto.Cipher c ^bytes ciphertext))))))
  (reset-meta!
    #'decrypt-pk
    (assoc
      {:arglists (clojure.core/list ['ciphertext (.withMeta 'key {:tag 'PublicKey})]),
       :column (int 1)}
      :name
      'decrypt-pk
      :ns
      *ns*))
  (def encrypt
   (fn encrypt
     ([plaintext cipher_name rawkey]
       (let [c (cipher cipher_name)]
         (.init
           ^javax.crypto.Cipher c
           (int Cipher/ENCRYPT_MODE)
           (javax.crypto.spec.SecretKeySpec. ^bytes rawkey ^java.lang.String cipher_name))
         (.doFinal ^javax.crypto.Cipher c (codec/string->bytes plaintext))))))
  (reset-meta!
    #'encrypt
    (assoc
      {:arglists (clojure.core/list ['plaintext 'cipher-name 'rawkey]), :column (int 1)}
      :name
      'encrypt
      :ns
      *ns*))
  (def decrypt
   (fn decrypt
     ([ciphertext cipher_name rawkey]
       (let [c (cipher cipher_name)]
         (.init
           ^javax.crypto.Cipher c
           (int Cipher/DECRYPT_MODE)
           (javax.crypto.spec.SecretKeySpec. ^bytes rawkey ^java.lang.String cipher_name))
         (codec/bytes->string (.doFinal ^javax.crypto.Cipher c ^bytes ciphertext))))))
  (reset-meta!
    #'decrypt
    (assoc
      {:arglists (clojure.core/list ['ciphertext 'cipher-name 'rawkey]), :column (int 1)}
      :name
      'decrypt
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.crypto" "random")
    {:tag java.security.SecureRandom, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.crypto" "random") (java.security.SecureRandom.))
  (defn random-bytes ([n] (let [dest (byte-array n)] (.nextBytes random ^bytes dest) dest)))
  (reset-meta!
    #'random-bytes
    (assoc {:arglists (clojure.core/list ['n]), :column (int 1)} :name 'random-bytes :ns *ns*))
  (def read-n
   (fn read_n
     ([bb n] (let [dest (byte-array n)] (.get ^java.nio.ByteBuffer bb ^bytes dest) dest))))
  (reset-meta!
    #'read-n
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'bb {:tag 'ByteBuffer}) 'n]), :column (int 1)}
      :name
      'read-n
      :ns
      *ns*))
  (def xor-arrays
   (fn xor_arrays
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
         result))))
  (reset-meta!
    #'xor-arrays
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'b1 {:tag 'bytes}) (.withMeta 'b2 {:tag 'bytes})]),
       :column (int 1)}
      :name
      'xor-arrays
      :ns
      *ns*))
  (def split-array
   (fn split_array
     ([b1]
       (let [b2 (random-bytes (java.lang.Integer/valueOf (int (count b1))))]
         [b2 (xor-arrays b1 b2)]))))
  (reset-meta!
    #'split-array
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'b1 {:tag 'bytes})]), :column (int 1)}
      :name
      'split-array
      :ns
      *ns*))
  (def split-key
   (fn split_key
     ([k]
       (map
         (fn fn__21805
           ([b]
             {:fragment (codec/bytes->string (codec/encode-64 b)),
              :algorithm (.getAlgorithm ^java.security.Key k)}))
         (split-array (.getEncoded ^java.security.Key k))))))
  (reset-meta!
    #'split-key
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'k {:tag 'Key})]), :column (int 1)}
      :name
      'split-key
      :ns
      *ns*))
  (def ser-key
   (fn ser_key
     ([k]
       {:key (codec/bytes->string (codec/encode-64 (.getEncoded ^java.security.Key k))),
        :alg (.getAlgorithm ^java.security.Key k)})))
  (reset-meta!
    #'ser-key
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'k {:tag 'Key})]), :column (int 1)}
      :name
      'ser-key
      :ns
      *ns*))
  (def deser-key
   (fn deser_key
     ([p__21809]
       (let [map__21810 p__21809
             map__21810 (if (seq? map__21810)
                          (if (next map__21810)
                            (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                              (to-array map__21810))
                            (if (seq map__21810) (first map__21810) {}))
                          map__21810)
             key (get map__21810 :key)
             alg (get map__21810 :alg)
             data (codec/decode-64 (codec/string->bytes key))]
         (javax.crypto.spec.SecretKeySpec.
           ^bytes data
           (int 0)
           (int (count data))
           ^java.lang.String alg)))))
  (reset-meta!
    #'deser-key
    (assoc
      {:arglists (clojure.core/list [{:keys ['key 'alg]}]), :column (int 1)}
      :name
      'deser-key
      :ns
      *ns*))
  (defn combine-fragments
    ([f1 f2]
      (let [b1 (codec/decode-64 (codec/string->bytes (:fragment f1)))
            b2 (codec/decode-64 (codec/string->bytes (:fragment f2)))]
        (javax.crypto.spec.SecretKeySpec. (xor-arrays b1 b2) (:algorithm f2)))))
  (reset-meta!
    #'combine-fragments
    (assoc
      {:arglists (clojure.core/list ['f1 'f2]), :column (int 1)}
      :name
      'combine-fragments
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.crypto" "hmac-length") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.crypto" "hmac-length")
    (memoize
      (fn fn__21813
        ([algorithm]
          (java.lang.Integer/valueOf
            (int (.getMacLength (Mac/getInstance ^java.lang.String algorithm))))))))
  (def calc-hmac
   (fn calc_hmac
     ([bbuf alg k]
       (let [mac (let [G__21815 (Mac/getInstance ^java.lang.String alg)]
                   (.init ^javax.crypto.Mac G__21815 ^java.security.Key k)
                   G__21815)
             result (.duplicate ^java.nio.ByteBuffer bbuf)]
         (.update ^javax.crypto.Mac mac ^java.nio.ByteBuffer result)
         (.doFinal ^javax.crypto.Mac mac)))))
  (reset-meta!
    #'calc-hmac
    (assoc
      {:arglists
       (clojure.core/list
         (.withMeta [(.withMeta 'bbuf {:tag 'ByteBuffer}) 'alg 'k] {:tag 'bytes})),
       :column (int 1)}
      :name
      'calc-hmac
      :ns
      *ns*))
  (def append-hmac
   (fn append_hmac
     ([bbuf alg k]
       (let [result (.duplicate ^java.nio.ByteBuffer bbuf)]
         (.position ^java.nio.ByteBuffer result (int (.limit ^java.nio.Buffer result)))
         (.limit
           ^java.nio.ByteBuffer result
           (int (+ (.limit ^java.nio.Buffer result) (hmac-length alg))))
         (.put ^java.nio.ByteBuffer result (calc-hmac bbuf alg k))
         (.flip ^java.nio.ByteBuffer result)))))
  (reset-meta!
    #'append-hmac
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'bbuf {:tag 'ByteBuffer}) 'alg 'k]),
       :column (int 1)}
      :name
      'append-hmac
      :ns
      *ns*))
  (def validate-hmac
   (fn validate_hmac
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
         (if (Arrays/equals ^bytes buf_hmac (calc-hmac result alg k))
           result
           (do
             (throw
               (ex-info
                 "HMAC integrity check failed"
                 #:db{:error :datomic.crypto/validate-hmac-failed}))
             nil))))))
  (reset-meta!
    #'validate-hmac
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'bbuf {:tag 'ByteBuffer}) 'alg 'k]),
       :column (int 1)}
      :name
      'validate-hmac
      :ns
      *ns*))
  (def crypt-bbuf
   (fn crypt_bbuf
     ([input k op extra]
       (let [ciph (cipher (str (.getAlgorithm ^java.security.Key k) "/CBC/PKCS5Padding"))
             iv_size (.getBlockSize ^javax.crypto.Cipher ciph)
             src (.duplicate ^java.nio.ByteBuffer input)
             ivbytes (let [G__21819 op]
                       (case
                         G__21819
                         :encrypt
                         (random-bytes (java.lang.Integer/valueOf (int iv_size)))
                         :decrypt
                         (read-n src (java.lang.Integer/valueOf (int iv_size)))))
             iv (javax.crypto.spec.IvParameterSpec. ^bytes ivbytes)]
         (.init
           ^javax.crypto.Cipher ciph
           (int
             (let [G__21820 op]
               (case G__21820 :encrypt Cipher/ENCRYPT_MODE :decrypt Cipher/DECRYPT_MODE)))
           ^java.security.Key k
           ^java.security.spec.AlgorithmParameterSpec iv)
         (let [n (.getOutputSize ^javax.crypto.Cipher ciph (int (.remaining ^java.nio.Buffer src)))
               dest (ByteBuffer/allocate (int (+ (+ iv_size n) extra)))]
           (when (= :encrypt op) (.put ^java.nio.ByteBuffer dest ^bytes ivbytes))
           (.doFinal ^javax.crypto.Cipher ciph ^java.nio.ByteBuffer src ^java.nio.ByteBuffer dest)
           (.flip ^java.nio.ByteBuffer dest))))
     ([input k op] (crypt-bbuf input k op 0))))
  (reset-meta!
    #'crypt-bbuf
    (assoc
      {:arglists
       (clojure.core/list
         ['input 'k 'op]
         [(.withMeta 'input {:tag 'ByteBuffer}) (.withMeta 'k {:tag 'SecretKey}) 'op 'extra]),
       :column (int 1)}
      :name
      'crypt-bbuf
      :ns
      *ns*))
  (defn sign
    ([algo priv arr]
      (let [signer (Signature/getInstance ^java.lang.String algo)]
        (.initSign ^java.security.Signature signer ^java.security.PrivateKey priv)
        (.update ^java.security.Signature signer ^bytes arr (int 0) (int (count arr)))
        (.sign ^java.security.Signature signer))))
  (reset-meta!
    #'sign
    (assoc
      {:arglists (clojure.core/list ['algo 'priv 'arr]), :column (int 1)}
      :name
      'sign
      :ns
      *ns*))
  (def verify
   (fn verify
     ([algo pub arr sig]
       (let [verifier (Signature/getInstance ^java.lang.String algo)]
         (.initVerify ^java.security.Signature verifier ^java.security.PublicKey pub)
         (.update ^java.security.Signature verifier ^bytes arr (int 0) (int (count arr)))
         (.verify ^java.security.Signature verifier ^bytes sig)))))
  (reset-meta!
    #'verify
    (assoc
      {:arglists (clojure.core/list ['algo (.withMeta 'pub {:tag 'PublicKey}) 'arr 'sig]),
       :column (int 1)}
      :name
      'verify
      :ns
      *ns*))
  (def keystore
   (fn keystore
     ([f password type]
       (with-open [is (jio/input-stream f)]
         (let [G__21824 (KeyStore/getInstance ^java.lang.String type)]
           (.load ^java.security.KeyStore G__21824 ^java.io.InputStream is ^chars password)
           G__21824)))
     ([f password] (keystore f password (KeyStore/getDefaultType)))))
  (reset-meta!
    #'keystore
    (assoc
      {:arglists (clojure.core/list ['f 'password] ['f 'password 'type]), :column (int 1)}
      :name
      'keystore
      :ns
      *ns*))
  (def load-private-key
   (fn load_private_key
     ([ks alias password]
       (.getKey ^java.security.KeyStore ks ^java.lang.String alias ^chars password))))
  (reset-meta!
    #'load-private-key
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'ks {:tag 'KeyStore}) 'alias 'password]),
       :column (int 1)}
      :name
      'load-private-key
      :ns
      *ns*))
  (def load-public-key
   (fn load_public_key
     ([ks alias]
       (.getPublicKey (.getCertificate ^java.security.KeyStore ks ^java.lang.String alias)))))
  (reset-meta!
    #'load-public-key
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'ks {:tag 'KeyStore}) 'alias]), :column (int 1)}
      :name
      'load-public-key
      :ns
      *ns*)))