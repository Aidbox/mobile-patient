(ns mobile-patient.screen.demographics
  (:require [mobile-patient.ui :as ui]
            [mobile-patient.nav :as nav]))


(defn DemographicsScreen [{:keys [navigation]}]
  [ui/view {:style {:flex 1}}
   [ui/text "Age"]
   [ui/input {:on-change-text #()}]
   [ui/text "Sex"]
   [ui/input {:on-change-text #()}]
   [ui/text "Address"]
   [ui/input {:on-change-text #()}]
   [ui/button {:title "Save" :on-press #(navigation.dispatch nav/tabs)}]])
