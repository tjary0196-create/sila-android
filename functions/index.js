const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

// ==================== MESSAGE REQUESTS ====================

/**
 * عند قبول طلب رسالة، أنشئ محادثة جديدة
 */
exports.onMessageRequestAccepted = functions.firestore
    .document('messageRequests/{requestId}')
    .onUpdate(async (change, context) => {
        const newValue = change.after.data();
        const previousValue = change.before.data();

        if (previousValue.status === 'pending' && newValue.status === 'accepted') {
            const { fromUid, toUid } = newValue;

            // جلب بيانات المستخدمين
            const [fromUserDoc, toUserDoc] = await Promise.all([
                db.collection('users').doc(fromUid).get(),
                db.collection('users').doc(toUid).get()
            ]);

            const fromUser = fromUserDoc.data();
            const toUser = toUserDoc.data();

            // إنشاء محادثة جديدة
            const chatRef = db.collection('chats').doc();
            await chatRef.set({
                chatId: chatRef.id,
                participants: [fromUid, toUid],
                participantNames: {
                    [fromUid]: fromUser.displayName,
                    [toUid]: toUser.displayName
                },
                participantPhotos: {
                    [fromUid]: fromUser.photoUrl || null,
                    [toUid]: toUser.photoUrl || null
                },
                unreadCount: { [fromUid]: 0, [toUid]: 0 },
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
                isGroup: false
            });

            // إرسال إشعار للطرف الآخر
            if (toUser.fcmToken) {
                await messaging.send({
                    token: toUser.fcmToken,
                    notification: {
                        title: 'طلب مراسلة مقبول',
                        body: `${fromUser.displayName} قبل طلب المراسلة`
                    },
                    data: {
                        type: 'request_accepted',
                        chatId: chatRef.id,
                        fromUid
                    }
                });
            }
        }

        return null;
    });

// ==================== NEW MESSAGE NOTIFICATIONS ====================

/**
 * عند إرسال رسالة جديدة، أرسل إشعار FCM
 */
exports.onNewMessage = functions.firestore
    .document('messages/{messageId}')
    .onCreate(async (snap, context) => {
        const message = snap.data();
        const { chatId, senderId, text, type } = message;

        // جلب بيانات المحادثة
        const chatDoc = await db.collection('chats').doc(chatId).get();
        if (!chatDoc.exists) return null;

        const chat = chatDoc.data();
        const recipientId = chat.participants.find(uid => uid !== senderId);
        if (!recipientId) return null;

        // التحقق من عدم وجود حظر
        const blockCheck = await db.collection('blockedUsers')
            .where('blockerUid', 'in', [senderId, recipientId])
            .where('blockedUid', 'in', [senderId, recipientId])
            .get();

        if (!blockCheck.empty) return null; // أحدهما محظور

        // تحديث unread count
        await db.collection('chats').doc(chatId).update({
            [`unreadCount.${recipientId}`]: admin.firestore.FieldValue.increment(1),
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });

        // جلب بيانات المرسل
        const senderDoc = await db.collection('users').doc(senderId).get();
        const sender = senderDoc.data();

        // جلب FCM token للمستلم
        const recipientDoc = await db.collection('users').doc(recipientId).get();
        const recipient = recipientDoc.data();

        if (!recipient || !recipient.fcmToken) return null;

        // التحقق من إعدادات الإشعارات
        if (recipient.publicProfile && recipient.publicProfile.muteNotifications) return null;

        const notificationBody = type === 'text' ? text : 'رسالة جديدة';

        await messaging.send({
            token: recipient.fcmToken,
            notification: {
                title: sender.displayName,
                body: notificationBody
            },
            data: {
                type: 'new_message',
                chatId,
                messageId: context.params.messageId,
                senderId
            },
            android: {
                priority: 'high',
                notification: {
                    channelId: 'messages',
                    sound: 'default'
                }
            }
        });

        return null;
    });

// ==================== USERNAME UNIQUENESS ====================

/**
 * التحقق من عدم تكرار اسم المستخدم عند الإنشاء
 */
exports.validateUsername = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'يجب تسجيل الدخول');
    }

    const { username } = data;
    if (!username || !/^[a-z0-9_.]{3,20}$/.test(username)) {
        throw new functions.https.HttpsError('invalid-argument', 'اسم المستخدم غير صالح');
    }

    const doc = await db.collection('usernames').doc(username.toLowerCase()).get();
    return { available: !doc.exists };
});

// ==================== BLOCK USER ====================

/**
 * عند حظر مستخدم، احذف أي محادثات مشتركة
 */
exports.onUserBlocked = functions.firestore
    .document('blockedUsers/{blockId}')
    .onCreate(async (snap, context) => {
        const { blockerUid, blockedUid } = snap.data();

        // حذف طلبات الرسائل المعلقة بينهما
        const requests = await db.collection('messageRequests')
            .where('fromUid', 'in', [blockerUid, blockedUid])
            .where('toUid', 'in', [blockerUid, blockedUid])
            .where('status', '==', 'pending')
            .get();

        const batch = db.batch();
        requests.docs.forEach(doc => {
            batch.update(doc.ref, { status: 'declined' });
        });

        await batch.commit();
        return null;
    });

// ==================== SESSION MANAGEMENT ====================

/**
 * تسجيل جلسة جديدة عند تسجيل الدخول
 */
exports.registerSession = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'يجب تسجيل الدخول');
    }

    const { deviceName, deviceModel, osVersion, appVersion, fcmToken } = data;
    const uid = context.auth.uid;

    const sessionRef = db.collection('sessions').doc();
    await sessionRef.set({
        sessionId: sessionRef.id,
        uid,
        deviceName: deviceName || 'Unknown Device',
        deviceModel: deviceModel || 'Unknown',
        osVersion: osVersion || 'Unknown',
        appVersion: appVersion || 'Unknown',
        ipAddress: context.rawRequest.ip || 'unknown',
        lastActive: admin.firestore.FieldValue.serverTimestamp(),
        isCurrent: true,
        fcmToken: fcmToken || ''
    });

    return { sessionId: sessionRef.id };
});

/**
 * تحديث FCM token
 */
exports.updateFcmToken = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'يجب تسجيل الدخول');
    }

    const { fcmToken } = data;
    await db.collection('users').doc(context.auth.uid).update({ fcmToken });

    return { success: true };
});

// ==================== CLEANUP FUNCTIONS ====================

/**
 * حذف الجلسات القديمة (تشغيل يومي)
 */
exports.cleanupOldSessions = functions.pubsub.schedule('every 24 hours').onRun(async (context) => {
    const thirtyDaysAgo = admin.firestore.Timestamp.fromDate(
        new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
    );

    const oldSessions = await db.collection('sessions')
        .where('lastActive', '<', thirtyDaysAgo)
        .get();

    const batch = db.batch();
    oldSessions.docs.forEach(doc => batch.delete(doc.ref));
    await batch.commit();

    console.log(`Deleted ${oldSessions.size} old sessions`);
    return null;
});
