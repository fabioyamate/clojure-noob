(ns clojure-noob.http-tls
  (:require [org.httpkit.client :as http]
            [less.awful.ssl :as ssl]
            [org.httpkit.sni-client :as sni-client])
  (:import [java.io ByteArrayInputStream FileInputStream]
           [java.security KeyStore]
           [javax.net.ssl SSLContext KeyManager TrustManager]))

;; refer https://github.com/aphyr/less-awful-ssl

(comment
  ;; simple http -request
  (:body @(http/get "https://httpbin.org/get"))

  ;; passing options
  @(http/get "https://httpbin.org/basic-auth/user/pass"
             {:timeout 3000
              :basic-auth ["user" "pass"]
              :query-params {:param "value" :param2 ["value1" "value2"]}
              :user-agent "User-Agent-string"
              :headers {"X-Header" "Value"}}
             (fn [{:keys [status headers body error]}] ;; asynchronous response handling
               (if error
                 (println "Failed, exception is " error)
                 (println "Async HTTP GET: " body))))

  ;; required tls
  @(http/get
    "https://service.local/discovery")
  ;;     {:method :get,
  ;;      :url "https://service.local/discovery"},
  ;;     :error #error {
  ;;     :cause "Received fatal alert: bad_certificate"
  ;;     :via
  ;;     [{:type javax.net.ssl.SSLHandshakeException
  ;;       :message "Received fatal alert: bad_certificate"
  ;;       :at [sun.security.ssl.Alert createSSLException "Alert.java" 131]}]
  ;;     :trace
  )

(defn file-input-stream
  "Convert a local file path to an InputStream"
  [path]
  (FileInputStream. ^String path))

(defn- client-p12 []
  (file-input-stream (str (System/getenv "HOME")
                          "/demo/certificates/demo.p12")))

(defn- ssl-p12-context-generator
  [p12]
  (let [key-store (KeyStore/getInstance "PKCS12")
        context (SSLContext/getInstance "TLS")
        password (char-array "demo-password")]
    (.load key-store p12 password)
    (.init context
           (into-array KeyManager [(ssl/key-manager key-store password)])
           (into-array TrustManager [(ssl/trust-manager (.load (KeyStore/getInstance "PKCS12") nil nil))])
           nil)
    context))

(comment
  (def result
    (let [eng (-> (client-p12)
                  (ssl-p12-context-generator)
                  ssl/ssl-context->engine)]
      @(http/request {:url "https://service.local/discovery"
                      :timeout 5000
                      :client @sni-client/default-client
                      :sslengine (doto eng
                                   (.setUseClientMode true))})))

  (:body result)
  )
