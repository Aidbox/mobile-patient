(ns mobile-patient.screen.login
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [mobile-patient.nav :as nav]))

(defn login-handler [login password dispatch]
  (if true
    (dispatch nav/demographics)
    (ui/alert "Error" "Invalid credentials"))
  )

(defn LoginScreen [{:keys [navigation]}]
  (let [login (atom "")
        password (atom "")]
    (fn []
      [ui/view {:style {:flex 1 :justify-content :center}}
       [ui/text "Login"]
       [ui/input {:on-change-text #(reset! login %)}]
       [ui/text "Password"]
       [ui/input {:on-change-text #(reset! password %)}]
       [ui/button {:title "LogIn" :on-press #(login-handler @login @password navigation.dispatch)}]])))
