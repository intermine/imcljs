(ns imcljs.auth-test
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.test :refer-macros [async deftest testing is]]
            [cljs.core.async :refer [<!]]
            [imcljs.env :refer [service]]
            [imcljs.fetch :as fetch]))

(deftest anon-token
  (testing "Able to retrieve anon token, even if passed an old/expired token"
    (async done
      (go
        (let [response (<! (fetch/session (assoc service :token "expired")))]
               ;; InterMine returns a 401 if you request a new token and pass an
               ;; old/bad/expired token. We can work around it though! 
          (is (not (= 401 (:status response))))
          (done))))))
