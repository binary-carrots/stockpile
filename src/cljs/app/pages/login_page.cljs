(ns app.pages.login-page
  (:require [helix.core :refer [defnc $ <>]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            [promesa.core :as p]
            [lambdaisland.fetch :as fetch]))

(defn login
  [user]
  (p/-> (fetch/post "http://localhost:3000/api/auth/login"
                    {:body user,
                     :credentials :include})
        (js->clj :keywordize-keys true)
        (println)))

(defnc login-form
  []
  (let [[user set-user] (hooks/use-state {:email "" :password ""})]
    (<>
     (d/form {:on-submit
              (fn [event]
                (.preventDefault event)
                (login user))}
             (d/label "Email")
             (d/input {:value (:email user)
                       :on-change #(set-user assoc :email (.. % -target -value))})
             (d/label "password")
             (d/input {:value (:password user)
                       :type "password"
                       :on-change #(set-user assoc :password (.. % -target -value))})
             (d/button {:type "submit"} "Sign in")))))

(defnc login-page []
  ($ login-form))