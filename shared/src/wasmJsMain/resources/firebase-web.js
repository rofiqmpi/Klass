import { initializeApp, getApps } from "https://www.gstatic.com/firebasejs/11.6.0/firebase-app.js";
import {
  getAuth,
  setPersistence,
  browserLocalPersistence,
  GoogleAuthProvider,
  EmailAuthProvider,
  signInWithPopup,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  reauthenticateWithCredential,
  updatePassword,
  linkWithCredential,
  signOut,
  deleteUser
} from "https://www.gstatic.com/firebasejs/11.6.0/firebase-auth.js";
import {
  getFirestore,
  collection,
  doc,
  getDoc,
  getDocs,
  setDoc,
  deleteDoc,
  query,
  where,
  writeBatch
} from "https://www.gstatic.com/firebasejs/11.6.0/firebase-firestore.js";

const cfg = window.__KLASS_FIREBASE_CONFIG__ || {};
const hasConfig = Boolean(cfg.apiKey && cfg.projectId);

let firebaseApp = null;
let auth = null;
const firestoreInstances = []; // [{ label: string, db: Firestore }]
let lastWorkingDbIndex = 0;

function initFirebase() {
  if (!hasConfig) {
    console.warn("[KlassWebFirebase] Missing __KLASS_FIREBASE_CONFIG__");
    return;
  }
  try {
    firebaseApp = getApps().length > 0 ? getApps()[0] : initializeApp({
      apiKey: cfg.apiKey,
      authDomain: cfg.authDomain || `${cfg.projectId}.firebaseapp.com`,
      projectId: cfg.projectId,
      storageBucket: cfg.storageBucket || `${cfg.projectId}.firebasestorage.app`,
      messagingSenderId: cfg.messagingSenderId || "",
      appId: cfg.appId || "1:000000000000:web:klass"
    });

    auth = getAuth(firebaseApp);
    setPersistence(auth, browserLocalPersistence).catch(() => {});

    const dbId = (cfg.databaseId || "").trim();
    if (dbId && dbId !== "(default)") {
      try {
        firestoreInstances.push({
          label: dbId,
          db: getFirestore(firebaseApp, dbId)
        });
      } catch (e) {
        console.warn("[KlassWebFirebase] Failed named Firestore init:", dbId, e);
      }
    }
    try {
      firestoreInstances.push({
        label: "(default)",
        db: getFirestore(firebaseApp)
      });
    } catch (e) {
      console.warn("[KlassWebFirebase] Failed default Firestore init:", e);
    }
  } catch (e) {
    console.error("[KlassWebFirebase] Initialization error:", e);
  }
}

initFirebase();

function getOrderedDbs() {
  if (firestoreInstances.length <= 1 || lastWorkingDbIndex <= 0 || lastWorkingDbIndex >= firestoreInstances.length) {
    return firestoreInstances.map((item, idx) => ({ ...item, idx }));
  }
  const ordered = [{ ...firestoreInstances[lastWorkingDbIndex], idx: lastWorkingDbIndex }];
  for (let i = 0; i < firestoreInstances.length; i++) {
    if (i !== lastWorkingDbIndex) {
      ordered.push({ ...firestoreInstances[i], idx: i });
    }
  }
  return ordered;
}

function sanitizeFirestoreValue(val) {
  if (val === null || val === undefined) return null;
  if (typeof val === "object") {
    if (typeof val.toMillis === "function") {
      return val.toMillis();
    }
    if (Array.isArray(val)) {
      return val.map(sanitizeFirestoreValue);
    }
    const out = {};
    for (const [k, v] of Object.entries(val)) {
      if (v !== undefined) {
        out[k] = sanitizeFirestoreValue(v);
      }
    }
    return out;
  }
  return val;
}

async function ensureAuthReady() {
  if (!auth) return null;
  try {
    if (typeof auth.authStateReady === "function") {
      await Promise.race([
        auth.authStateReady(),
        new Promise((resolve) => setTimeout(resolve, 2500))
      ]);
    }
  } catch (_) {}
  return auth.currentUser || null;
}

function mapAuthError(err) {
  const code = (err && err.code) || "";
  const msg = (err && err.message) || String(err || "Unknown error");
  if (code === "auth/popup-closed-by-user" || code === "auth/cancelled-popup-request") {
    return "সাইন-ইন বাতিল করা হয়েছে";
  }
  if (code === "auth/popup-blocked") {
    return "ব্রাউজার পপ-আপ ব্লক করেছে। পপ-আপ অনুমতি দিয়ে আবার চেষ্টা করুন।";
  }
  if (code === "auth/unauthorized-domain") {
    return `এই ডোমেইন (${window.location.hostname}) Firebase Authentication Authorized Domains-এ যুক্ত করা নেই। Firebase Console > Authentication > Settings > Authorized domains-এ যুক্ত করুন।`;
  }
  if (code === "auth/network-request-failed") {
    return "NO_INTERNET";
  }
  if (code === "auth/operation-not-allowed") {
    return "ERROR_OPERATION_NOT_ALLOWED";
  }
  if (code === "auth/email-already-in-use") {
    return "ERROR_EMAIL_ALREADY_IN_USE";
  }
  if (code === "auth/invalid-credential" || code === "auth/wrong-password" || code === "auth/user-not-found") {
    return "ERROR_INVALID_CREDENTIAL";
  }
  return msg;
}

let toastTimer = null;
function showWebToast(message) {
  try {
    if (!message) return;
    let el = document.getElementById("klass-web-toast");
    if (!el) {
      el = document.createElement("div");
      el.id = "klass-web-toast";
      el.style.cssText = [
        "position:fixed",
        "left:50%",
        "bottom:28px",
        "transform:translateX(-50%)",
        "z-index:99999",
        "max-width:86vw",
        "padding:10px 18px",
        "border-radius:999px",
        "background:rgba(15,23,42,0.92)",
        "color:#F8FAFC",
        "font-family:system-ui,-apple-system,sans-serif",
        "font-size:13px",
        "font-weight:500",
        "box-shadow:0 8px 24px rgba(15,23,42,0.28)",
        "pointer-events:none",
        "transition:opacity 0.2s ease",
        "opacity:0",
        "text-align:center"
      ].join(";");
      document.body.appendChild(el);
    }
    el.textContent = String(message);
    el.style.opacity = "1";
    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
      el.style.opacity = "0";
    }, 2800);
  } catch (e) {
    console.log(message);
  }
}

window.__klassFirebase = {
  isConfigured() {
    return Boolean(hasConfig && firestoreInstances.length > 0);
  },

  showToast(message) {
    showWebToast(message);
  },

  randomSaltHex() {
    const arr = new Uint8Array(16);
    if (window.crypto && typeof window.crypto.getRandomValues === "function") {
      window.crypto.getRandomValues(arr);
    } else {
      for (let i = 0; i < arr.length; i++) arr[i] = Math.floor(Math.random() * 256);
    }
    return Array.from(arr, (b) => b.toString(16).padStart(2, "0")).join("");
  },

  async pbkdf2Hash(saltHex, password, onDone) {
    try {
      const cleanHex = String(saltHex || "").trim();
      const saltBytes = new Uint8Array(cleanHex.length / 2);
      for (let i = 0; i < saltBytes.length; i++) {
        saltBytes[i] = parseInt(cleanHex.substring(i * 2, i * 2 + 2), 16) || 0;
      }
      const enc = new TextEncoder();
      const keyMaterial = await window.crypto.subtle.importKey(
        "raw",
        enc.encode(password),
        "PBKDF2",
        false,
        ["deriveBits"]
      );
      const bits = await window.crypto.subtle.deriveBits(
        {
          name: "PBKDF2",
          salt: saltBytes,
          iterations: 120000,
          hash: "SHA-256"
        },
        keyMaterial,
        256
      );
      const hex = Array.from(new Uint8Array(bits), (b) => b.toString(16).padStart(2, "0")).join("");
      onDone(JSON.stringify({ ok: true, hash: hex }));
    } catch (e) {
      onDone(JSON.stringify({ ok: false, error: String(e) }));
    }
  },

  async authStatus(onDone) {
    try {
      const user = await ensureAuthReady();
      if (!user) {
        onDone(JSON.stringify({ signedIn: false }));
        return;
      }
      const providers = Array.isArray(user.providerData) ? user.providerData.map((p) => p.providerId) : [];
      onDone(JSON.stringify({
        signedIn: true,
        uid: user.uid || "",
        email: (user.email || "").trim().toLowerCase(),
        displayName: user.displayName || "",
        photoUrl: user.photoURL || "",
        providers
      }));
    } catch (e) {
      onDone(JSON.stringify({ signedIn: false, error: String(e) }));
    }
  },

  async signInWithGoogle(onDone) {
    if (!hasConfig || !auth) {
      onDone(JSON.stringify({ ok: false, error: "WEB_FIREBASE_CONFIG_MISSING" }));
      return;
    }
    try {
      const provider = new GoogleAuthProvider();
      provider.setCustomParameters({ prompt: "select_account" });
      const res = await signInWithPopup(auth, provider);
      const user = res.user;
      if (!user || !user.email) {
        onDone(JSON.stringify({ ok: false, error: "Google account email পাওয়া যায়নি" }));
        return;
      }
      onDone(JSON.stringify({
        ok: true,
        uid: user.uid || "",
        email: user.email.trim().toLowerCase(),
        name: user.displayName || user.email.split("@")[0],
        photoUrl: user.photoURL || ""
      }));
    } catch (e) {
      onDone(JSON.stringify({ ok: false, error: mapAuthError(e) }));
    }
  },

  async signOutGoogle(onDone) {
    try {
      if (auth) await signOut(auth);
      onDone(JSON.stringify({ ok: true }));
    } catch (e) {
      onDone(JSON.stringify({ ok: false, error: String(e) }));
    }
  },

  async signInWithEmail(email, password, onDone) {
    if (!hasConfig || !auth) {
      onDone(JSON.stringify({ ok: false, errorCode: "WEB_FIREBASE_CONFIG_MISSING", error: "WEB_FIREBASE_CONFIG_MISSING" }));
      return;
    }
    try {
      const res = await signInWithEmailAndPassword(auth, email.trim().toLowerCase(), password);
      const user = res.user;
      onDone(JSON.stringify({
        ok: true,
        uid: user ? user.uid : "",
        email: user && user.email ? user.email.trim().toLowerCase() : email.trim().toLowerCase(),
        name: (user && user.displayName) || ""
      }));
    } catch (e) {
      const mapped = mapAuthError(e);
      onDone(JSON.stringify({ ok: false, errorCode: (e && e.code) || "", error: mapped }));
    }
  },

  async createUserWithEmail(email, password, onDone) {
    if (!hasConfig || !auth) {
      onDone(JSON.stringify({ ok: false, error: "WEB_FIREBASE_CONFIG_MISSING" }));
      return;
    }
    try {
      const res = await createUserWithEmailAndPassword(auth, email.trim().toLowerCase(), password);
      const user = res.user;
      onDone(JSON.stringify({
        ok: true,
        uid: user ? user.uid : "",
        email: user && user.email ? user.email.trim().toLowerCase() : email.trim().toLowerCase()
      }));
    } catch (e) {
      onDone(JSON.stringify({ ok: false, errorCode: (e && e.code) || "", error: mapAuthError(e) }));
    }
  },

  async setOrUpdateAuthPassword(email, newPassword, currentPassword, hasCurrent, onDone) {
    if (!hasConfig || !auth) {
      onDone(JSON.stringify({ ok: false, error: "WEB_FIREBASE_CONFIG_MISSING" }));
      return;
    }
    try {
      const user = await ensureAuthReady();
      if (!user || (user.email || "").trim().toLowerCase() !== email.trim().toLowerCase()) {
        onDone(JSON.stringify({ ok: false, error: "NO_ACTIVE_USER" }));
        return;
      }
      const hasPasswordProvider = Array.isArray(user.providerData) &&
        user.providerData.some((p) => p.providerId === EmailAuthProvider.PROVIDER_ID);

      if (hasPasswordProvider) {
        if (hasCurrent && currentPassword) {
          try {
            const cred = EmailAuthProvider.credential(email.trim().toLowerCase(), currentPassword);
            await reauthenticateWithCredential(user, cred);
          } catch (reErr) {
            onDone(JSON.stringify({ ok: false, reauthFailed: true, error: mapAuthError(reErr) }));
            return;
          }
        }
        await updatePassword(user, newPassword);
      } else {
        const cred = EmailAuthProvider.credential(email.trim().toLowerCase(), newPassword);
        await linkWithCredential(user, cred);
      }
      onDone(JSON.stringify({ ok: true }));
    } catch (e) {
      onDone(JSON.stringify({ ok: false, error: mapAuthError(e) }));
    }
  },

  async deleteCurrentAuthUser(onDone) {
    try {
      const user = await ensureAuthReady();
      if (user) await deleteUser(user);
      onDone(JSON.stringify({ ok: true }));
    } catch (e) {
      onDone(JSON.stringify({ ok: false, error: mapAuthError(e) }));
    }
  },

  async getDoc(collectionName, docId, tryAllDbs, onDone) {
    if (firestoreInstances.length === 0) {
      onDone(JSON.stringify({ ok: false, error: "WEB_FIREBASE_CONFIG_MISSING" }));
      return;
    }
    await ensureAuthReady();
    let lastErr = null;
    for (const { db, idx } of getOrderedDbs()) {
      try {
        const snap = await getDoc(doc(db, collectionName, docId));
        lastWorkingDbIndex = idx;
        if (snap.exists()) {
          onDone(JSON.stringify({
            ok: true,
            exists: true,
            id: snap.id,
            data: sanitizeFirestoreValue(snap.data())
          }));
          return;
        }
        if (!tryAllDbs) {
          onDone(JSON.stringify({ ok: true, exists: false, id: docId }));
          return;
        }
      } catch (e) {
        lastErr = e;
      }
    }
    if (lastErr) {
      onDone(JSON.stringify({ ok: false, exists: false, error: String(lastErr.message || lastErr) }));
    } else {
      onDone(JSON.stringify({ ok: true, exists: false, id: docId }));
    }
  },

  async getCollection(collectionName, mergeAllDbs, onDone) {
    if (firestoreInstances.length === 0) {
      onDone(JSON.stringify({ ok: false, docs: [], error: "WEB_FIREBASE_CONFIG_MISSING" }));
      return;
    }
    await ensureAuthReady();
    const docMap = new Map();
    let anySuccess = false;
    let lastErr = null;
    for (const { db, idx } of getOrderedDbs()) {
      try {
        const snap = await getDocs(collection(db, collectionName));
        anySuccess = true;
        lastWorkingDbIndex = idx;
        snap.forEach((d) => {
          if (!docMap.has(d.id)) {
            docMap.set(d.id, { id: d.id, data: sanitizeFirestoreValue(d.data()) });
          } else if (mergeAllDbs) {
            const incoming = sanitizeFirestoreValue(d.data());
            const existing = docMap.get(d.id);
            const inTs = Number(incoming.updated_at || incoming.generated_at || incoming.resolved_at || 0);
            const exTs = Number((existing.data && (existing.data.updated_at || existing.data.generated_at || existing.data.resolved_at)) || 0);
            if (inTs >= exTs) {
              docMap.set(d.id, { id: d.id, data: incoming });
            }
          }
        });
        if (!mergeAllDbs && docMap.size > 0) break;
      } catch (e) {
        lastErr = e;
      }
    }
    if (anySuccess) {
      onDone(JSON.stringify({ ok: true, docs: Array.from(docMap.values()) }));
    } else {
      onDone(JSON.stringify({ ok: false, docs: [], error: String((lastErr && lastErr.message) || lastErr || "Query failed") }));
    }
  },

  async queryEquals(collectionName, field, value, mergeAllDbs, onDone) {
    if (firestoreInstances.length === 0) {
      onDone(JSON.stringify({ ok: false, docs: [], error: "WEB_FIREBASE_CONFIG_MISSING" }));
      return;
    }
    await ensureAuthReady();
    const docMap = new Map();
    let anySuccess = false;
    let lastErr = null;
    for (const { db, idx } of getOrderedDbs()) {
      try {
        const q = query(collection(db, collectionName), where(field, "==", value));
        const snap = await getDocs(q);
        anySuccess = true;
        lastWorkingDbIndex = idx;
        snap.forEach((d) => {
          if (!docMap.has(d.id)) {
            docMap.set(d.id, { id: d.id, data: sanitizeFirestoreValue(d.data()) });
          }
        });
        if (!mergeAllDbs && docMap.size > 0) break;
      } catch (e) {
        lastErr = e;
      }
    }
    if (anySuccess) {
      onDone(JSON.stringify({ ok: true, docs: Array.from(docMap.values()) }));
    } else {
      onDone(JSON.stringify({ ok: false, docs: [], error: String((lastErr && lastErr.message) || lastErr || "Query failed") }));
    }
  },

  async setDoc(collectionName, docId, dataJson, merge, writeAllDbs, onDone) {
    if (firestoreInstances.length === 0) {
      onDone(JSON.stringify({ ok: false, error: "WEB_FIREBASE_CONFIG_MISSING" }));
      return;
    }
    await ensureAuthReady();
    let parsed = {};
    try {
      parsed = JSON.parse(dataJson);
    } catch (e) {
      onDone(JSON.stringify({ ok: false, error: "Invalid JSON payload" }));
      return;
    }
    let okCount = 0;
    let lastErr = null;
    for (const { db, idx } of getOrderedDbs()) {
      try {
        await setDoc(doc(db, collectionName, docId), parsed, merge ? { merge: true } : {});
        lastWorkingDbIndex = idx;
        okCount++;
        if (!writeAllDbs) break;
      } catch (e) {
        lastErr = e;
      }
    }
    if (okCount > 0) {
      onDone(JSON.stringify({ ok: true }));
    } else {
      onDone(JSON.stringify({ ok: false, error: String((lastErr && lastErr.message) || lastErr || "Firestore write failed") }));
    }
  },

  async deleteDoc(collectionName, docId, writeAllDbs, onDone) {
    if (firestoreInstances.length === 0) {
      onDone(JSON.stringify({ ok: false, error: "WEB_FIREBASE_CONFIG_MISSING" }));
      return;
    }
    await ensureAuthReady();
    let okCount = 0;
    let lastErr = null;
    for (const { db, idx } of getOrderedDbs()) {
      try {
        await deleteDoc(doc(db, collectionName, docId));
        lastWorkingDbIndex = idx;
        okCount++;
        if (!writeAllDbs) break;
      } catch (e) {
        lastErr = e;
      }
    }
    if (okCount > 0) {
      onDone(JSON.stringify({ ok: true }));
    } else {
      onDone(JSON.stringify({ ok: false, error: String((lastErr && lastErr.message) || lastErr || "Firestore delete failed") }));
    }
  },

  async deleteCollection(collectionName, writeAllDbs, onDone) {
    if (firestoreInstances.length === 0) {
      onDone(JSON.stringify({ ok: false, error: "WEB_FIREBASE_CONFIG_MISSING" }));
      return;
    }
    await ensureAuthReady();
    let okCount = 0;
    let lastErr = null;
    for (const { db, idx } of getOrderedDbs()) {
      try {
        const snap = await getDocs(collection(db, collectionName));
        const docs = snap.docs || [];
        for (let i = 0; i < docs.length; i += 450) {
          const chunk = docs.slice(i, i + 450);
          const batch = writeBatch(db);
          chunk.forEach((d) => batch.delete(d.ref));
          await batch.commit();
        }
        lastWorkingDbIndex = idx;
        okCount++;
        if (!writeAllDbs) break;
      } catch (e) {
        lastErr = e;
      }
    }
    if (okCount > 0) {
      onDone(JSON.stringify({ ok: true }));
    } else {
      onDone(JSON.stringify({ ok: false, error: String((lastErr && lastErr.message) || lastErr || "Failed deleting collection") }));
    }
  },

  async batchSetDocs(collectionName, docsArrayJson, merge, writeAllDbs, onDone) {
    if (firestoreInstances.length === 0) {
      onDone(JSON.stringify({ ok: false, error: "WEB_FIREBASE_CONFIG_MISSING" }));
      return;
    }
    await ensureAuthReady();
    let items = [];
    try {
      items = JSON.parse(docsArrayJson);
    } catch (e) {
      onDone(JSON.stringify({ ok: false, error: "Invalid batch JSON" }));
      return;
    }
    let okCount = 0;
    let lastErr = null;
    for (const { db, idx } of getOrderedDbs()) {
      try {
        for (let i = 0; i < items.length; i += 450) {
          const chunk = items.slice(i, i + 450);
          const batch = writeBatch(db);
          for (const item of chunk) {
            const ref = doc(db, collectionName, item.id);
            if (merge) {
              batch.set(ref, item.data, { merge: true });
            } else {
              batch.set(ref, item.data);
            }
          }
          await batch.commit();
        }
        lastWorkingDbIndex = idx;
        okCount++;
        if (!writeAllDbs) break;
      } catch (e) {
        lastErr = e;
      }
    }
    if (okCount > 0) {
      onDone(JSON.stringify({ ok: true, count: items.length }));
    } else {
      onDone(JSON.stringify({ ok: false, error: String((lastErr && lastErr.message) || lastErr || "Batch write failed") }));
    }
  },

  pickImageDataUri(onDone) {
    try {
      const input = document.createElement("input");
      input.type = "file";
      input.accept = "image/*";
      input.style.display = "none";
      input.onchange = () => {
        const file = input.files && input.files[0];
        if (!file) {
          input.remove();
          onDone("");
          return;
        }
        const reader = new FileReader();
        reader.onload = () => {
          const img = new Image();
          img.onload = () => {
            const maxDim = 360;
            const scale = Math.min(maxDim / img.width, maxDim / img.height, 1);
            const w = Math.max(1, Math.round(img.width * scale));
            const h = Math.max(1, Math.round(img.height * scale));
            const canvas = document.createElement("canvas");
            canvas.width = w;
            canvas.height = h;
            const ctx = canvas.getContext("2d");
            ctx.drawImage(img, 0, 0, w, h);
            const dataUri = canvas.toDataURL("image/jpeg", 0.78);
            input.remove();
            onDone(dataUri);
          };
          img.onerror = () => {
            input.remove();
            onDone("");
          };
          img.src = String(reader.result || "");
        };
        reader.readAsDataURL(file);
      };
      document.body.appendChild(input);
      input.click();
    } catch (e) {
      onDone("");
    }
  },

  async translateText(text, sourceLang, targetLang, onDone) {
    try {
      const url = `https://translate.googleapis.com/translate_a/single?client=gtx&sl=${encodeURIComponent(sourceLang)}&tl=${encodeURIComponent(targetLang)}&dt=t&q=${encodeURIComponent(text)}`;
      const res = await fetch(url);
      if (!res.ok) {
        onDone("");
        return;
      }
      const data = await res.json();
      const segs = Array.isArray(data && data[0]) ? data[0] : [];
      const out = segs.map((s) => (Array.isArray(s) && s[0]) || "").join("");
      onDone(out);
    } catch (_) {
      onDone("");
    }
  },

  downloadHtmlRoutine(filename, title, htmlBody) {
    try {
      const docHtml = `<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><title>${title}</title><style>body{font-family:system-ui,-apple-system,sans-serif;padding:24px;color:#0f172a;}table{width:100%;border-collapse:collapse;margin-top:16px;}th,td{border:1px solid #cbd5e1;padding:8px 10px;text-align:left;font-size:13px;}th{background:#eff6ff;color:#1e3a8a;}</style></head><body>${htmlBody}</body></html>`;
      const blob = new Blob([docHtml], { type: "text/html;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      a.remove();
      setTimeout(() => URL.revokeObjectURL(url), 4000);
      return true;
    } catch (_) {
      return false;
    }
  }
};
