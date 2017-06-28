(ns mobile-patient.screen.login
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [cljs.core.async :as a :refer [<!]]
            [mobile-patient.ui :as ui]
            [mobile-patient.nav :as nav]
            [mobile-patient.lib.ajax :refer [ajax]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))


;; For test purposes.
(def user-map
  {"Mary" {:id "Mary" :ref {:id "76b0930f-8a5f-49d9-b9cb-94bb76ecf7c9" :resourceType "Patient"}}
   "Brian" {:id "Brian" :ref {:id "6ee2281a-2d3a-4084-b0fa-9e3b73446122" :resourceType "Patient"}}
   "practitioner" {:id "practitioner" :ref {:id "26fa1663-e8a2-4ee8-90d5-de632fc2f68a" :resourceType "Practitioner"}}
   "patient" {:id "patient" :ref {:id "fe0ecce6-a577-4cde-8c02-f7c482111de8" :resourceType "Patient"}}})

(defn LoginScreen [{:keys [navigation]}]
  (let [login (r/atom "patient")
        password (r/atom "")
        loading (r/atom false)]
    (fn []
      [ui/view {:style {:flex 1 :justify-content :center :margin 30}}
       (if @loading [ui/activity-indicator])
       [ui/input {:placeholder "Login" :value @login :on-change-text #(reset! login %)}]
       [ui/input {:placeholder "Password" :value @password :on-change-text #(reset! password %)}]
       [ui/button {:title "Log In" :on-press #(do
                                                #_(rf/dispatch [:login @login @password])
                                                (if-let [user (get user-map @login)]
                                                  (do
                                                    (rf/dispatch-sync [:set-user user])
                                                    (rf/dispatch [:set-current-screen :main]))
                                                  (ui/alert "Login" "User not found")))}]])))
