package com.ic.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.ic.notification.entity.Notify;
import com.ic.notification.entity.Token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final FirebaseMessaging firebaseMessaging;

    private final static Firestore db = FirestoreClient.getFirestore();

    public List<String> getTokens(String userId) throws Exception {
        DocumentSnapshot task = db.collection("tokens").document(userId).get().get();
        Token token = task.toObject(Token.class);
        return token.getTokens();
    }

    private String getBody(String orderId, int status) {
        String msg = String.format("Đơn hàng %s", orderId);
        switch (status) {
            case 0:
                msg += " cần được xác nhận";
                break;
            case 1:
                msg += " đang được giao";
                break;
            case 2:
                msg += " đã được giao";
                break;
            case -1:
                msg += " đã được hủy";
                break;
            default:
                break;
        }
        return msg;
    }

    public String sendNotification(Map<String, String> params) throws Exception {
        String userId = (String) params.get("userId");
        String orderId = (String) params.get("orderId");
        String status = (String) params.get("status");
        String title = "Trạng thái đơn hàng";
        String body = getBody(orderId, Integer.valueOf(status));
        Notify notify = new Notify(title, body, System.currentTimeMillis(), true, userId);

        List<String> registrationTokens = getTokens(userId);
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(registrationTokens)
                .setNotification(notification)
                .build();
        saveNotify(notify);
        BatchResponse batchResponse = null;
        try {
            batchResponse = firebaseMessaging.sendMulticast(message);
        } catch (FirebaseMessagingException e) {
            return e.getMessage();
        }
        if (batchResponse.getFailureCount() > 0) {
            List<SendResponse> responses = batchResponse.getResponses();
            List<String> failedTokens = new ArrayList<>();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    failedTokens.add(registrationTokens.get(i));
                }
            }
            log.info("List of tokens that caused failures: " + failedTokens);
        }
        return "OK";
    }

    public void saveNotify(Notify notify) {
        DocumentReference documentReference = db.collection("notifications").document();
        notify.setId(documentReference.getId());
        documentReference.set(notify);
    }
}