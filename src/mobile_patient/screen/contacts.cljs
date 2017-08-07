(ns mobile-patient.screen.contacts
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.model.patient :as patient-model]))


(defn row-component [{:keys [item index]} navigation]
  [ui/touchable-highlight {:style {:border-color "#E9E9EF"
                                   :border-width 1
                                   :border-top-width (if (zero? index) 0 1)
                                   :border-bottom-width 0
                                   :padding 10
                                   :background-color "#ffffff"}
                           :on-press (fn []
                                       (dispatch [:create-chat [(:username item)]])
                                       (navigation.goBack nil))}
   [ui/view {:style {:flex-direction :row
                     :flex 1
                     :justify-content :space-between
                     :padding-left 15
                     :padding-right 10
                     }}
    [ui/avatar (str "data:image/png;base64," (patient-model/get-photo item))
               20]
    [ui/text {:style {:flex 1
                      :margin-left 15
                      :text-align "left"
                      :font-size 16
                      :padding-top 4
                      :color "#333"
                      :font-weight :bold}} (patient-model/get-official-name item)]
    [ui/icon {:name "add" :size 30 :color "#FF485C"}]]])


(defn header-component []
  [ui/text {:style {:color color/pink
                    :font-weight :bold
                    :padding 25}}
      "Select a Person"])

(defn ContactsScreen [{:keys [navigation]}]
  (let [contacts @(subscribe [:contacts])]
    (ui/show-remote-data
     @(subscribe [:contacts])
     (fn [data]
       [ui/flat-list {:style {:background-color :white}
                           :data (clj->js data)
                           :key-extractor #(.-id %)
                           :ListHeaderComponent (r/reactify-component header-component)
                           :render-item (fn [row] (r/as-element [row-component (js->clj row :keywordize-keys true) navigation]))}]))))
