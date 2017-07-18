(ns mobile-patient.screen.login
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]))


(defn LoginScreen [{:keys [navigation]}]
  (let [login (r/atom "patient@com.com")
        password (r/atom "patient")
        loading (rf/subscribe [:get-in [:spinner :login]])]
    (fn []
      [ui/view {:style {:flex 1 :justify-content :center :margin 30}}
       (if @loading [ui/activity-indicator])

       [ui/input {:placeholder "Login"
                  :value @login
                  :on-change-text #(reset! login %)
                  :style {:height 40}}]

       [ui/input {:placeholder "Password"
                  :value @password
                  :on-change-text #(reset! password %)
                  :style {:height 40}}]

       [ui/button {:title "Log In"
                   :on-press #(rf/dispatch [:login @login @password])}]])))
