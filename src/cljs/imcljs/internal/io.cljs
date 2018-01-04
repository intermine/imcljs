(ns imcljs.internal.io
  (:require [cljs-http.client :refer [post get delete]]
            [imcljs.internal.utils :refer [scrub-url]]
            [imcljs.internal.defaults
             :refer [url wrap-get-defaults wrap-request-defaults
                     wrap-post-defaults wrap-auth wrap-accept
                     wrap-delete-defaults transform-if-successful]]))

;(defn body-
;  "Parses the body of the web service response.
;  Accepts an optional transformation function for further parsing,
;  such as :model to pull  out {:body {:model ..."
;  [request-chan & [xform]]
;  (go
;    (let [response (:body (<! request-chan))]
;      (.log js/console "response" response)
;      ;(close! request-chan)
;      (if xform (xform response) response))))

(defn post-body-wrapper-
  "Returns the results of queries as table rows."
  [path {:keys [root token model]} options & [xform]]
  (post (url root path)
        (-> options ; Blank request map
            ;(wrap-accept)
            (wrap-request-defaults xform) ; Add defaults such as with-credentials false?
            ;(wrap-post-defaults options model) ; Add form params
            (wrap-auth token)
            ; Stringify the clojure body to a JSON data structure
            ; This should still work when sending plain/text rather than application/json
            (update :body (comp js/JSON.stringify clj->js)))))


(defn post-wrapper-
  "Returns the results of queries as table rows."
  [path {:keys [root token model]} options & [xform]]
  (post (url root path)
        (-> {} ; Blank request map
            ;(wrap-accept)
            (wrap-request-defaults xform) ; Add defaults such as with-credentials false?
            (wrap-post-defaults options model) ; Add form params
            (wrap-auth token))))

(defn delete-wrapper-
  "Returns the results of queries as table rows."
  [path {:keys [root token model]} options & [xform]]
  (delete (url root path)
          (-> {} ; Blank request map
              ;(wrap-accept)
              (wrap-request-defaults xform) ; Add defaults such as with-credentials false?
              (wrap-delete-defaults options) ; Add form params
              (wrap-auth token))))

(defn request-wrapper-
  "Returns the results of queries as table rows."
  [path {:keys [root token model headers]} options & [xform]]
  (get (url root path)
       (-> {} ; Blank request map
           ;(wrap-accept)
           (wrap-request-defaults xform) ; Add defaults such as with-credentials false?
           (wrap-get-defaults options) ; Add query params
           (wrap-auth token))))


(defn basic-auth-wrapper-
  "Returns the results of queries as table rows."
  [path {:keys [root token model]} options & [xform]]
  ; clj-http expects basic-auth params as [username password
  ; cljs-http expects basic-auth params as {:username username :password password}
  (let [basic-auth-params options]
    (get (url root path)
         (-> {} ; Blank request map
             (wrap-request-defaults xform) ; Add defaults such as with-credentials false?
             (assoc :basic-auth basic-auth-params)))))

(defmulti restful (fn [method & args] method))


;(defmethod restful :raw [method path {:keys [root token model] :as service} request & [xform]]
;  (let [http-fn (case method :get get :post post :delete delete)]
;    (http-fn (url root path) request)))

(defmethod restful :post [method path service options & [xform]]
  ;(body- (post-wrapper- path service options) xform)
  (post-wrapper- path service options xform))

(defmethod restful :raw [_ method path {:keys [root token model] :as service} request & [xform]]
  ;(body- (post-wrapper- path service options) xform)
  (let [http-fn (case method :get get :post post :delete delete)]
    (http-fn (url root path) (wrap-request-defaults request xform))))

(defmethod restful :post-body [method path service options & [xform]]
  ;(body- (post-wrapper- path service options) xform)
  (post-body-wrapper- path service options xform))


(defmethod restful :get [method path service options & [xform]]
  ;(body- (request-wrapper- path service options) xform)
  (request-wrapper- path service options xform))

(defmethod restful :delete [method path service options & [xform]]
  (delete-wrapper- path service options xform))

; TODO
; rather than make an edge case (:basic-auth is not an HTTP verb!),
; make custom clj/s-http options param
(defmethod restful :basic-auth [method path service options & [xform]]
  (basic-auth-wrapper- path service options xform))





