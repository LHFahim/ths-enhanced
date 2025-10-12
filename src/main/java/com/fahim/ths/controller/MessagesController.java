package com.fahim.ths.controller;

import com.fahim.ths.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessagesController {

    @FXML private Label currentUserLabel;
    @FXML private TextField peerEmailField;
    @FXML private ListView<String> messagesList;
    @FXML private TextArea composeArea;

    private Integer meId;           // current user id
    private String meName;          // current user name
    private Integer peerId;         // conversation partner id
    private String peerName;        // partner name (for display)

    @FXML
    public void initialize() {
        // read session user set at login
        Map<String, Object> me = Session.getCurrentUser();
        if (me != null) {
            meId = ((Number) me.get("id")).intValue();
            meName = String.valueOf(me.get("name"));
            currentUserLabel.setText(meName + " (id=" + meId + ")");
        } else {
            currentUserLabel.setText("(not logged in)");
        }
    }

    @FXML
    private void onLoadThread() {
        try {
            String email = peerEmailField.getText() == null ? "" : peerEmailField.getText().trim();
            if (email.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "enter peer email").show();
                return;
            }

            ThsClient c = new ThsClient("127.0.0.1", ServerMain.PORT);

            // resolve peer by email
            Response find = c.send("FIND_USER_BY_EMAIL", Map.of("email", email));
            if (!find.ok) {
                new Alert(Alert.AlertType.ERROR, "no user found for email").show();
                return;
            }
            peerId = ((Number) find.data.get("id")).intValue();
            peerName = String.valueOf(find.data.get("name"));

            // load thread
            Response r = c.send("LIST_MESSAGES", Map.of("userA", meId, "userB", peerId));
            if (!r.ok) { new Alert(Alert.AlertType.ERROR, "failed to load: " + r.error).show(); return; }

            renderMessages((List<Map<String,Object>>) r.data.get("messages"));
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "error: " + ex.getMessage()).show();
            ex.printStackTrace();
        }
    }

    @FXML
    private void onSend() {
        try {
            if (peerId == null) {
                new Alert(Alert.AlertType.WARNING, "load a conversation first (enter email, press Load).").show();
                return;
            }
            String body = composeArea.getText() == null ? "" : composeArea.getText().trim();
            if (body.isEmpty()) return;

            ThsClient c = new ThsClient("127.0.0.1", ServerMain.PORT);
            Response s = c.send("SEND_MESSAGE", Map.of(
                    "sender_id", meId,
                    "receiver_id", peerId,
                    "body", body
            ));
            if (!s.ok) { new Alert(Alert.AlertType.ERROR, "send failed: " + s.error).show(); return; }

            composeArea.clear();

            // refresh thread
            Response r = c.send("LIST_MESSAGES", Map.of("userA", meId, "userB", peerId));
            if (r.ok) renderMessages((List<Map<String,Object>>) r.data.get("messages"));

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "error: " + ex.getMessage()).show();
            ex.printStackTrace();
        }
    }

    private void renderMessages(List<Map<String,Object>> msgsRaw) {
        List<String> lines = new ArrayList<>();
        for (Map<String,Object> m : msgsRaw) {
            int sender = ((Number) m.get("sender")).intValue();
            String body = String.valueOf(m.get("body"));
            String sentAt = String.valueOf(m.get("sent_at"));
            boolean mine = sender == meId;
            String who = mine ? "You" : (peerName != null ? peerName : "Peer");
            lines.add(who + ": " + body + "  [" + sentAt + "]");
        }
        messagesList.getItems().setAll(lines);
        if (!lines.isEmpty()) messagesList.scrollTo(lines.size() - 1);
    }
}
