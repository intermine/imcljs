(ns imcljs.internal.io
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [imcljs.query :as q]
            [imcljs.internal.defaults :refer [url wrap-request-defaults
                                              wrap-post-defaults
                                              wrap-auth]]
            [imcljs.internal.utils :refer [assert-args]]))

(def method-map {:get client/get
                 :post client/post
                 :delete client/delete})

(def default-options {:as :json})
;:throw-exceptions false


(def username-password (juxt :username :password))

(defn wrap-defaults
  ([] (wrap-defaults {}))
  ([m] (merge default-options m)))


(defn basic-auth-xform
  "Transform a :basic-auth username/password map into a vec"
  [options]
  (if (contains? options :basic-auth)
    (update options :basic-auth username-password)
    options))

(defn wrap-token
  [service options]
  (update options :headers assoc "Authorization" (str "Token: " (:token options))))

(defn xform-options
  "Apply all transformations to options map"
  [service options]
  (-> options
      wrap-defaults
      basic-auth-xform
      (wrap-token service)))

; If we have a transform function (such as :token) then extract
; that value from the body if the request was successful
(defn parse-response
  "Perform a transformation function (if present) on a successful HTTP request"
  [xform response]
  (if xform
    (if (<= 200 (:status response) 300)
      (xform (:body response))
      response)
    response))

(defn wrap-basic-auth [request]
  (if (contains? request :basic-auth)
    (update request :basic-auth (juxt :username :password))
    request))


(defn get-body-wrapper-
  [path {:keys [root token model]} options & [xform]]
  (parse-response xform (client/get (url root path)
                                    (-> options ; Blank request map
                                        ;(wrap-accept)
                                        (wrap-request-defaults xform) ; Add defaults such as with-credentials false?
                                        ; If we have basic auth options then convert them from the cljs-http to clj-http format
                                        wrap-basic-auth
                                        ;(wrap-post-defaults options model) ; Add form params
                                        (wrap-auth token)
                                        (merge {:as :json})))))

(defn post-body-wrapper-
  [path {:keys [root token model]} options & [xform]]
  (parse-response xform (client/post (url root path)
                                    (-> options ; Blank request map
                                        ;(wrap-accept)
                                        (wrap-request-defaults xform) ; Add defaults such as with-credentials false?
                                        ; If we have basic auth options then convert them from the cljs-http to clj-http format
                                        (wrap-post-defaults options model)
                                        wrap-basic-auth
                                        ;(wrap-post-defaults options model) ; Add form params
                                        (wrap-auth token)
                                        (merge {:as :json})))))


; Perform an HTTP request
(defn request
  [method path {:keys [root token model] :as service} options & [xform]]
  (let [http-fn (get method-map method)]
    (parse-response xform (http-fn (url root path) (xform-options service options)))))


; We're using a multimethod with just one default method because clj-http
; seems to handle parameters generically regardless of protocol.
; However, cljs-http and therefore uses a multimethod to wrap parameters appropriately
(defmulti restful
  (fn [method & args]
    (apply assert-args method args)
    method))

(defmethod restful :raw [_ method path {:keys [root token model] :as service} request & [xform]]
  (let [http-fn (get method-map method)]
    (parse-response xform (http-fn (url root path) (merge request (merge {:as :json}))))))

(defmethod restful :get [method path service options & [xform]]
  (get-body-wrapper- path service (merge options {:accept :json}) xform))

(defmethod restful :post [method path service options & [xform]]
  (post-body-wrapper- path service (merge options {:accept :json}) xform))

(defmethod restful :basic-auth [method path service options & [xform]]
  (request method path service options xform))

(defmethod restful :default [method path service options & [xform]]
  (request method path service options xform))
