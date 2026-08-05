import * as functions from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import fetch from "node-fetch";

/**
 * Holds the imgbb API key as a managed secret (set via:
 *   firebase functions:secrets:set IMGBB_API_KEY
 * ) — it never appears in client code, build output, or the APK.
 */
const IMGBB_API_KEY = defineSecret("IMGBB_API_KEY");

// Extremely simple in-memory per-instance rate limit: at most 5 uploads
// per uid per rolling minute. This is a best-effort speed bump against a
// single compromised/malicious client hammering the endpoint — it is not
// a substitute for a durable, cross-instance limiter (e.g. Firestore- or
// Redis-backed) if this ever needs to be bulletproof at scale.
const uploadTimestamps = new Map<string, number[]>();
const MAX_UPLOADS_PER_MINUTE = 5;
const WINDOW_MS = 60_000;
const MAX_IMAGE_BYTES = 5 * 1024 * 1024; // 5 MB, matches the client-side check

function isRateLimited(uid: string): boolean {
  const now = Date.now();
  const timestamps = (uploadTimestamps.get(uid) ?? []).filter((t) => now - t < WINDOW_MS);
  timestamps.push(now);
  uploadTimestamps.set(uid, timestamps);
  return timestamps.length > MAX_UPLOADS_PER_MINUTE;
}

export const uploadProfilePhoto = functions.onCall(
  { secrets: [IMGBB_API_KEY], enforceAppCheck: true },
  async (request) => {
    // 1) Must be a signed-in Firebase user — no anonymous/unauthenticated calls.
    if (!request.auth) {
      throw new functions.HttpsError("unauthenticated", "يجب تسجيل الدخول لرفع صورة.");
    }
    const uid = request.auth.uid;

    // 2) Basic per-user rate limiting.
    if (isRateLimited(uid)) {
      throw new functions.HttpsError(
        "resource-exhausted",
        "عدد محاولات رفع الصور كبير جداً، حاول لاحقاً."
      );
    }

    // 3) Validate the payload shape before doing any network work.
    const base64Image = request.data?.image;
    if (typeof base64Image !== "string" || base64Image.length === 0) {
      throw new functions.HttpsError("invalid-argument", "لا توجد بيانات صورة صالحة.");
    }
    // Base64 inflates size by ~4/3 — this bounds the *decoded* size to MAX_IMAGE_BYTES.
    const approxDecodedBytes = Math.floor((base64Image.length * 3) / 4);
    if (approxDecodedBytes > MAX_IMAGE_BYTES) {
      throw new functions.HttpsError("invalid-argument", "حجم الصورة كبير جداً.");
    }

    // 4) Forward to imgbb using the server-held key — the client never sees this key.
    const form = new URLSearchParams();
    form.set("key", IMGBB_API_KEY.value());
    form.set("image", base64Image);

    const response = await fetch("https://api.imgbb.com/1/upload", {
      method: "POST",
      body: form,
    });

    if (!response.ok) {
      throw new functions.HttpsError("unavailable", "فشل الاتصال بخدمة رفع الصور.");
    }

    const json = (await response.json()) as {
      success?: boolean;
      data?: { url?: string };
    };

    if (!json.success || !json.data?.url) {
      throw new functions.HttpsError("internal", "فشل رفع الصورة.");
    }

    return { url: json.data.url };
  }
);
