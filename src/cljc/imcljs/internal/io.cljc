(ns imcljs.internal.io

  (:require #?(:cljs [cljs-http.client :refer [post get delete]]
               :clj [clj-http.client :refer [post get delete]])
                    [imcljs.internal.utils :refer [scrub-url]]
                    [imcljs.internal.defaults :refer [url wrap-get-defaults wrap-request-defaults
                                                      wrap-post-defaults wrap-auth wrap-accept
                                                      wrap-delete-defaults]]))

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
  [path {:keys [root token model]} options & [xform]]
  (get (url root path)
       (-> {} ; Blank request map
           ;(wrap-accept)
           (wrap-request-defaults xform) ; Add defaults such as with-credentials false?
           (wrap-get-defaults options) ; Add query params
           (wrap-auth token))))

(defn basic-auth-wrapper-
  "Returns the results of queries as table rows."
  [path {:keys [root token model]} options & [xform]]
  (get (url root path)
       (-> {} ; Blank request map
           (wrap-request-defaults xform) ; Add defaults such as with-credentials false?
           (assoc :basic-auth options))))

(defmulti restful (fn [method & args] method))

(defmethod restful :post [method path service options & [xform]]
  ;(body- (post-wrapper- path service options) xform)
  (post-wrapper- path service options xform))


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

