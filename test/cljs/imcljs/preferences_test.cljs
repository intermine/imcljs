(ns imcljs.list-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [cljs.core.async :refer [<!]]
            [imcljs.fetch :as fetch]
            [imcljs.save :as save]
            [imcljs.env :refer [service]]
            [imcljs.auth :as auth]))

(deftest fetch-and-save-preferences
  (testing "Should be able to get and change user preferences"
    (async done
      (go
        (let [login-response   (<! (auth/login service "test_user@mail_account" "secret"))
              token            (get-in login-response [:output :token])
              service+auth     (assoc service :token token)
              _add-preferences (<! (save/preferences service+auth {:test true}))
              get-preferences  (<! (fetch/preferences service+auth))]
          (is (= {:test "true"} get-preferences))
          (done))))))
