(ns imcljs.internal.io
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [cljs-http.client :refer [post get delete]]
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

(defmulti restful identity)

(defmethod restful :post [method path service options & [xform]]
  ;(body- (post-wrapper- path service options) xform)
  (post-wrapper- path service options xform)
  )

(defmethod restful :get [method path service options & [xform]]
  ;(body- (request-wrapper- path service options) xform)
  (request-wrapper- path service options xform)
  )

(defmethod restful :delete [method path service options & [xform]]
  (delete-wrapper- path service options xform))