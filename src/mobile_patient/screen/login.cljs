(ns mobile-patient.screen.login
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [mobile-patient.nav :as nav]))


(defn LoginScreen [{:keys [navigation]}]
  [ui/view {:style {:flex 1
                    :justify-content :center}}
   [ui/text "Login"]
   [ui/input {:on-change-text #()}]
   [ui/text "Password"]
   [ui/input {:on-change-text #()}]
   [ui/button {:title "Log In" :on-press #(navigation.dispatch nav/demographics)}]])
