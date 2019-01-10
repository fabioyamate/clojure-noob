(ns clojure-noob.basic-crypto
  (:require [crypto.random :as crypto.random]
            [pandect.algo.sha256 :as sha256])
  (:import [java.security KeyPairGenerator KeyFactory]))

(defn- generate-keys
  [algorithm bits]
  (let [gen (doto (KeyPairGenerator/getInstance algorithm)
              (.initialize (int bits)))
        pair (.genKeyPair gen)]
    (vector
     (.getPrivate pair)
     (.getPublic pair))))

(comment
  (crypto.random/bytes 10)
  (crypto.random/base64 10)
  (crypto.random/base32 10)
  (crypto.random/hex 10)
  (crypto.random/url-part 10)

  ;; hash - integrity
  (sha256/sha256 "foo")
  (sha256/sha256 (.getBytes "foo" "UTF-8"))
  (sha256/sha256-file "project.clj")
  (sha256/sha256 (java.io.File. "project.clj"))
  (sha256/sha256 (java.io.FileInputStream. "project.clj"))

  (sha256/sha256-bytes "foo")

  ;; hmac = integrity and authenticity by a password
  (sha256/sha256-hmac "foo" "secret")

  ;; signature, verify that a given message came from the exepected
  ;; trusted entity
  ;; content doesn't matter (open / encrypted) the point here is to
  ;; ensure that the content is not tampered
  (def rsa-keys (generate-keys "RSA" 4096))

  (def signature-message (sha256/sha256-rsa "hello" (first rsa-keys)))

  (sha256/sha256-rsa-verify "hello" signature-message (second rsa-keys))

  ;; good explanation about encryption
  ;; https://crypto.stackexchange.com/questions/33864/how-is-the-message-digest-related-to-signatures-and-encryption

  ;; - content encryption / encyrption-at-rest is generally done with password

  ;; for transfering content between peers you need both encryption and signature

  ;; TODO check keyczar for encryption/decryption
  )
