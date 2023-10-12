(ns app.core
  (:require
   [helix.core :refer [$]]
   ["react-dom/client" :as rdom]
   [app.pages.login-page :refer [login-page]]
   [app.pages.users-page :refer [users-page]]))

;; start your app with your favorite React renderer
(defonce root (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init
  []
  (.render root ($ users-page)))
