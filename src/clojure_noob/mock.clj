(ns clojure-noob.mock
  (:import [org.mockserver.client.server MockServerClient]
           [org.mockserver.model HttpRequest HttpResponse Header StringBody]
           [org.mockserver.matchers Times]))

(defn request []
  (HttpRequest/request))

(defn exact [body]
  (StringBody. body))

(defn exactly [count]
  (Times/exactly count))

(defn response []
  (HttpResponse/response))

(defn header [k & args]
  (Header. k args))

(-> (MockServerClient. "127.0.0.1" 1080)
  (.when (doto (request)
           (.withMethod "POST")
           (.withPath "/login")
           (.withBody (exact "{username: 'foo', password: 'bar'}")))
         (exactly 1))
  (.respond (doto (response)
              (.withStatusCode (int 401))
              (.withHeaders [(header "Content-Type" "application/json; charset=utf-8")])
              (.withBody "{ message: 'incorrect username and password combination' }"))))
