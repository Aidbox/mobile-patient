(ns mobile-patient.screen.login
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [cljs.core.async :as a :refer [<!]]
            [mobile-patient.ui :as ui]
            [mobile-patient.nav :as nav]
            [mobile-patient.lib.ajax :refer [ajax]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

;; (defn check-user-exists [login password dispatch users]
;;   (go
;;     (let [id (->> users
;;                  (filter #(= login (:email %)))
;;                  first
;;                  :id)]
;;      (if id
;;        (condp #(= %1 (first %2)) (<! (ajax :get (str "https://bismi.aidbox.io/users/" id) {}))
;;          :error :>> #(ui/alert "Error" "login not found")
;;          :response :>> #(do
;;                           (rf/dispatch [:set-user id])
;;                           (dispatch nav/demographics))
;;          )))))

;; (defn login-handler [loading login password dispatch]
;;   (if (or (empty? login)
;;           (empty? password))
;;     (ui/alert "" "All fields are required")
;;     (go
;;       (reset! loading true)
;;       (let [[status content] (<! (ajax :get "https://bismi.aidbox.io/users" {}))]
;;         (reset! loading false)
;;         (if (= status :response)
;;           (check-user-exists login password dispatch content)
;;           (ui/alert "Error" content)
;;           )))))

;; (defn LoginScreen [{:keys [navigation]}]
;;   (let [login (atom "")
;;         password (atom "")
;;         loading (r/atom false)]
;;     (fn []
;;       [ui/view {:style {:flex 1 :justify-content :center :margin 30}}
;;        (if @loading [ui/activity-indicator])
;;        [ui/input {:placeholder "Login" :on-change-text #(reset! login %)}]
;;        [ui/input {:placeholder "Password" :on-change-text #(reset! password %)}]
;;        [ui/button {:title "Log In" :on-press #(login-handler loading @login @password navigation.dispatch)}]])))

;; For test purposes. Uncomment code above for prod.

(def user-map
  {"Mary" {:id "Mary" :ref {:id "76b0930f-8a5f-49d9-b9cb-94bb76ecf7c9" :resourceType "Patient"}}
   "Brian" {:id "Brian" :ref {:id "6ee2281a-2d3a-4084-b0fa-9e3b73446122" :resourceType "Patient"}}
   "practitioner" {:id "patient" :ref {:id "26fa1663-e8a2-4ee8-90d5-de632fc2f68a" :resourceType "Practitioner"}}
   "patient" {:id "patient" :ref {:id "fe0ecce6-a577-4cde-8c02-f7c482111de8" :resourceType "Patient"}}})

(defn LoginScreen [{:keys [navigation]}]
  (let [login (atom "")
        password (atom "")
        loading (r/atom false)]
    (fn []
      [ui/view {:style {:flex 1 :justify-content :center :margin 30}}
       (if @loading [ui/activity-indicator])
       [ui/input {:placeholder "Login" :on-change-text #(reset! login %)}]
       [ui/input {:placeholder "Password" :on-change-text #(reset! password %)}]
       [ui/button {:title "Log In" :on-press #(do
                                                (if-let [user (get user-map @login)]
                                                  (do
                                                    (rf/dispatch [:set-user user])
                                                    (navigation.dispatch nav/tabs))
                                                  (ui/alert "Login" "User not found")))}]])))

