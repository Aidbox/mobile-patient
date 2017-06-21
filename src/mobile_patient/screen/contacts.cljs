(ns mobile-patient.screen.contacts
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [mobile-patient.nav :as nav]))


(defn row-component [{:keys [item]}]
  [ui/touchable-highlight {:style {:border-color "#E9E9EF"
                                   :border-width 1
                                   ;;:border-top-width 0
                                   :padding 10
                                   :background-color "#ffffff"}
                           :on-press (fn [])}
   [ui/view {:style {:flex-direction :row :justify-content :space-between}}
    [ui/text {:style {:text-align "left"}} (:title item)]
    [ui/icon {:name "chevron-right" :size 30 :color "#FF485C"}]]])


(defn ContactsScreen [{:keys [navigation]}]
  [ui/flat-list {:data #js [#js {:key "a" :title "aaa"}
                            #js {:key "b" :title "bbb"}]
                 :render-item (fn [row] (r/as-element [row-component (js->clj row :keywordize-keys true)]))}])
