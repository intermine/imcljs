(ns imcljs.internal.io
  (:require [clj-http.client :as client]
            [imcljs.internal.defaults :refer [url]]))

(def method-map {:get client/get
                 :post client/post
                 :delete client/delete})

(def default-options {:as :json
                      ;:throw-exceptions false
                      })

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

(defn xform-options
  "Apply all transformations to options map"
  [options]
  (-> options
      wrap-defaults
      basic-auth-xform))

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

; Perform an HTTP request
(defn request
  [method path {:keys [root token model]} options & [xform]]
  (let [http-fn (get method-map method)]
    (parse-response xform (http-fn (url root path) (xform-options options)))))

; We're using a multimethod with just one default method because clj-http
; seems to handle parameters generically regardless of protocol.
; However, cljs-http and therefore uses a multimethod to wrap parameters appropriately
(defmulti restful (fn [method & args] method))

(defmethod restful :default [method path service options & [xform]]
  (request method path service options xform))

