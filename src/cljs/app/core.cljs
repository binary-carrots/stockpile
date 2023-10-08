(ns app.core
  (:require
   [helix.core :refer [defnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [promesa.core :as p]
   ["react-dom/client" :as rdom]
   [lambdaisland.fetch :as fetch]))

(defn sign-in
  [user]
  (p/-> (fetch/post "http://localhost:3000/api/auth/login"
                    {:body user,
                     :headers {:with-credentials true,
                               :credentials "include"}})
        (js->clj :keywordize-keys true)
        (println)))

(defnc sign-in-form
  []
  (let [[user set-user] (hooks/use-state {:email "" :password ""})]
    (<>
     (d/form {:on-submit (fn [event]
                           (.preventDefault event)
                           (sign-in user))}
             (d/label "Email")
             (d/input {:value (:email user)
                       :on-change #(set-user assoc :email (.. % -target -value))})
             (d/label "password")
             (d/input {:value (:password user)
                       :type "password"
                       :on-change #(set-user assoc :password (.. % -target -value))})
             (d/button {:type "submit"} "Sign in")))))

(defnc user-list-item [{:keys [user]}]
  (d/li
   (d/div
    (d/p (d/strong "ID: ") (:id user))
    (d/p (d/strong "Name: ") (:username user))
    (d/p (d/strong "Email: ") (:email user)))))

(defnc users-list []
  (let [[users set-users] (hooks/use-state {})
        _ (hooks/use-effect
           :once
           (p/-> (fetch/get "http://localhost:3000/api/admin/users" {:accept :json
                                                                     :content-type :json})
                 (:body)
                 (js->clj :keywordize-keys true)
                 set-users))]
    (<>
     (if (:users users)
       (d/ul
        (map-indexed
         (fn [i user]
           ($ user-list-item {:key i :user user}))
         (:users users)))
       (d/p "error")))))

(defnc app []
  (let [[state set-state] (hooks/use-state {})]
    (d/div
     (d/h1 "Stockpile")
     ($ sign-in-form)
     ($ users-list))))

;; start your app with your favorite React renderer
(defonce root (rdom/createRoot (js/document.getElementById "app")))


(defn ^:export init
  []
  (.render root ($ app)))