(ns mobile-patient.screen.chats
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [reagent.core :as r]
            [clojure.string :as str]
            [mobile-patient.color :as color]
            [mobile-patient.model.chat :as chat-model]
            [mobile-patient.model.user :as user-model]
            [mobile-patient.component.person-row :refer [person-row-component]]))

(def font-size 16)

(defn group-row-component [{:keys [item index]}
                          navigation
                          on-press
                          icon-name]
  (let [chat item
        ;;other-id (chat-model/other-participant-id chat @(subscribe [:domain-user]))
        ;;other-user @(subscribe [:user-by-id other-id])
        chat-name (str (user-model/get-official-name @(subscribe [:domain-user])) " PG")]
    [ui/touchable-highlight {:style {:padding 10}
                             :on-press #(on-press item navigation)
                             :underlay-color "white"}
     [ui/view {:style {:flex 1
                       :flex-direction :row
                       :justify-content :space-between
                       :margin-top 10
                       :margin-bottom 10
                       :padding-left 15
                       :padding-right 10}}
      [ui/view {:style {:flex 0.9}}
       [ui/text {:style {:color "black"
                         :font-weight :bold
                         :font-size font-size}}
        chat-name]

       [ui/text {:style {:color "#919291"
                         :font-size 13}}
        (str/join ", " (->> (:participants chat)
                            (map :id)
                            (map (fn [x] @(subscribe [:user-by-id x])))
                            (map user-model/get-official-name)))]]

      [ui/view {:style {:justify-content :center}}
       [ui/icon { ;; :style {:flex 0.1
                 ;;         :align-self :center}
                 :name "chevron-right"
                 :size 30
                 :color color/pink}]]]]))


(defn on-group-press-callback [group navigation]
  (let [chat group]
    (dispatch [:set-chat chat])
    (navigation.navigate "Chat" #js{:chat-name (:name chat)})))

(defn on-person-press-callback [person navigation]
  (let [chat (:chat person)]
    (dispatch [:set-chat chat])
    (navigation.navigate "Chat" #js{:chat-name (:name chat)})))


(defn Header [text]
  [ui/text {:style {:margin-left 20
                    :margin-top 20
                    :margin-bottom 20
                    :color color/pink
                    :font-size font-size
                    :font-weight :bold}}
        text])

(defn ChatsScreen [{:keys [navigation]}]
  (let [i-am-patient true
        groups @(subscribe [:practice-groups])
        people @(subscribe [:personal-chats])]
    (fn [_]
      [ui/scroll-view {:style {:background-color "white"
                               :flex 1}}
       ;; groups
       [Header "Practice Groups"]
       (if (empty? groups)
         [ui/text {:style {:margin-left 20}} "No chats yet"]
         [ui/flat-list {:style {:background-color :white}
                        :data (clj->js groups)
                        :key-extractor #(.-id %)
                        :render-item (fn [row]
                                       (r/as-element [group-row-component
                                                      (js->clj row :keywordize-keys true)
                                                      navigation
                                                      on-group-press-callback
                                                      "chevron-right"]))}])
       ;; persons
       [Header (if i-am-patient "Practitioners" "Patients")]
       (if (empty? people)
         [ui/text {:style {:margin-left 20}} "No chats yet"]
         [ui/flat-list {:style {:background-color :white}
                        :data (clj->js people)
                        :key-extractor #(.-id %)
                        :render-item (fn [row]
                                       (r/as-element [person-row-component
                                                      (js->clj row :keywordize-keys true)
                                                      navigation
                                                      on-person-press-callback
                                                      "chevron-right"]))}])])))
