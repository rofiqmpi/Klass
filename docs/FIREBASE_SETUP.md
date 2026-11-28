# Klass Firebase production checklist

The app stays on Firebase (Auth, Firestore, FCM, Firebase AI Logic). Firestore is the right database
for Klass (collection data, filtered queries, security rules), so no Realtime Database migration is needed.

## 1. Deploy rules
```
firebase deploy --only firestore:rules
```
Run `firestore.test.js` against the emulator first (`firebase emulators:exec --only firestore "node --test firestore.test.js"`).

## 2. Protect the database (Firebase Console)
- Firestore > Disaster recovery: enable **Point-in-time recovery** and a daily/weekly **backup schedule**.
- App Check: register the Android app (Play Integrity for release, Debug provider for debug), then **Enforce**
  for Firestore, Authentication and Firebase AI Logic after confirming traffic is verified.
- Authentication > Settings: enable email enumeration protection and keep password policy at 8+ characters.
- Project settings: add both release SHA-1/SHA-256 fingerprints listed in the README.
- Budgets & alerts: set a monthly budget alert on the Google Cloud project (Gemini calls are billable).

## 3. Indexes
Current queries use a single field (`email`, `category`), which Firestore indexes automatically.
Add composite indexes to `firestore.indexes.json` only when a new query combines filter + sort.

## 4. Known privacy gap (needs an app change, tracked as a follow-up)
Any signed-in user can read whole `students` / `teachers` / `users` documents, including fields marked
"Private" in the UI. Firestore cannot hide single fields. Fix: move private fields (phone, DOB, address,
parents' names) to `students/{email}/private/profile` readable only by the owner and admins, and keep the
public document limited to name, photo, department, blood group. This requires changes in
`RoleConfig.android.kt` and the profile screens, so it ships separately.

## 5. Zed AI knowledge base
`zed_knowledge` accepts client-written `verified: true`. Treat entries as unmoderated until a Cloud Function
(or admin-only review flow) sets `verified`.
