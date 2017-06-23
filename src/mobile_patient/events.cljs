(ns mobile-patient.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [reg-event-db after reg-event-fx reg-fx
                          dispatch dispatch-sync subscribe reg-sub-raw]]
   [mobile-patient.ui :as ui]
   [clojure.spec.alpha :as s]
   [mobile-patient.db :as db :refer [app-db]]))

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Views----------------------------------------------------------

(def ds (ui/ReactNative.ListView.DataSource. #js{:rowHasChanged (fn[a b] false)}))

(def row-comp (r/reactify-component
               (fn [props]
                 (let [row (props :row)
                       navigation (props :navigation)
                       chat @row]
                   [ui/touchable-highlight
                    {:style {:border-top-width 1 :border-color "#000"}
                     :on-press #(do
                                  (dispatch [:set-chat chat])
                                  (navigation.navigate "Chat" #js{:chat-name (:name chat)}))}
                    [ui/text (:name chat)]]))))

(defonce get-chats-timer-id (atom nil))
(def dt (atom (js/Date.)))

(defn ChatsScreen [{:keys [navigation]}]
  (js/clearInterval @get-chats-timer-id)
  (reset! get-chats-timer-id (js/setInterval #(dispatch [:get-chats])) 3000)
  (fn [_]
    (let [chats (subscribe [:chats])
          source (map #(r/atom %) @chats)]
      [ui/view
       [ui/list-view {:dataSource (.cloneWithRows ds (clj->js source))
                      :enableEmptySections true
                      :render-row (fn [row]
                                    (r/create-element
                                     row-comp
                                     #js{:row row
                                         :navigation navigation}))}]])))

(defn ChatScreen [_]
  (fn [_]
    [ui/view
     [ui/text "Test1"]]))

;; -- Subscriptions----------------------------------------------------------

(reg-sub-raw
 :chats
 (fn [db _] (reaction (:chats @db))))

(reg-sub-raw
 :chat
 (fn [db _] (reaction (:chat @db))))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
 :initialize-db
 (fn [_ _]
   app-db))

(reg-event-db
 :set-chat
 (fn [db [_ chat]]
   (assoc db :chat chat)))

(reg-event-db
 :on-chats
 (fn [db [_ value]]
   (let [chats (map :resource (:entry value))]
     (if (not (= (:chats db) chats))
       (assoc db :chats chats)
       db))))

(defn parms->query [parms]
  (str "?"
       (clojure.string/join
        "&"
        (map #(str
               (js/encodeURIComponent (name (first %)))
               "="
               (js/encodeURIComponent (second %)))
             parms))))

(reg-fx
 :fetch
 (fn [{:keys [:uri :opts :success]}]
   (let [base-url (subscribe [:get-in [:config :base-url]])]
     (-> (js/fetch (str @base-url uri (parms->query (:parms opts)))
                   (clj->js (merge {:method "GET"
                                    :headers {"content-type" "application/json"}}
                                   opts)))
         (.then #(.json %))
         (.then
          (fn [response]
            (dispatch [success (js->clj response :keywordize-keys true)])))
         (.catch #(println %)))
     {})))

(reg-event-fx
 :get-chats
 (fn [cofx [_]]
   (let [user (subscribe [:get-in [:user]])]
     {:fetch {:uri "/Chat"
              :success :on-chats
              :opts {:parms {:participant @user}}}})))
