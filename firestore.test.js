const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DATABASE_ID = "ai-studio-android-klass-8e28a555-75a6-4902-af9c-0b216450601b";
const ADMIN_EMAIL = "mdrofiqmpi@gmail.com";
const USER_EMAIL = "student@mpi.edu.bd";
const OTHER_EMAIL = "other@mpi.edu.bd";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function getDb(context) {
  return context.firestore(DATABASE_ID);
}

test("Public books, classrooms, and zed_knowledge allow read; sensitive collections block unauthenticated read", async () => {
  const unauthDb = getDb(testEnv.unauthenticatedContext());
  await assertSucceeds(unauthDb.collection("books").get());
  await assertSucceeds(unauthDb.collection("classrooms").get());
  await assertSucceeds(unauthDb.collection("zed_knowledge").get());
  await assertSucceeds(unauthDb.collection("routines").doc("master_6m_published").get());

  await assertFails(unauthDb.collection("users").get());
  await assertFails(unauthDb.collection("students").get());
  await assertFails(unauthDb.collection("teachers").get());
  await assertFails(unauthDb.collection("admins").get());
  await assertFails(unauthDb.collection("routines").doc("master_6m").get());
});

test("zed_knowledge: authenticated user can create valid non-PII fact; rejects unauth, email PII, invalid domain, update, and cross-user delete", async () => {
  const aliceUid = "alice_uid_1";
  const bobUid = "bob_uid_2";

  const aliceDb = getDb(testEnv.authenticatedContext(aliceUid, { email: USER_EMAIL }));
  const bobDb = getDb(testEnv.authenticatedContext(bobUid, { email: OTHER_EMAIL }));
  const adminDb = getDb(testEnv.authenticatedContext("admin_uid", { email: ADMIN_EMAIL }));
  const unauthDb = getDb(testEnv.unauthenticatedContext());

  const validId = "zk_1794500000_1234";
  const validPayload = {
    id: validId,
    topic: "MPI Computer Lab",
    fact: "CST Lab 2 is located on the 3rd floor of Academic Building.",
    domain: "CLASSROOMS",
    author_uid: aliceUid,
    author_role: "STUDENT",
    verified: true,
    created_at: 1794500000000,
  };

  // Unauthenticated create rejected
  await assertFails(unauthDb.collection("zed_knowledge").doc(validId).set(validPayload));

  // Valid Kotlin-mirrored payload succeeds
  await assertSucceeds(aliceDb.collection("zed_knowledge").doc(validId).set(validPayload));

  // Reject email PII inside fact or topic
  await assertFails(
    aliceDb.collection("zed_knowledge").doc("zk_pii_fact").set({
      ...validPayload,
      id: "zk_pii_fact",
      fact: "Contact coordinator at someone@gmail.com for details.",
    })
  );

  // Reject spoofed author_uid
  await assertFails(
    aliceDb.collection("zed_knowledge").doc("zk_spoof_uid").set({
      ...validPayload,
      id: "zk_spoof_uid",
      author_uid: bobUid,
    })
  );

  // Reject disallowed domain
  await assertFails(
    aliceDb.collection("zed_knowledge").doc("zk_bad_domain").set({
      ...validPayload,
      id: "zk_bad_domain",
      domain: "PRIVATE_PII",
    })
  );

  // Reject updates (immutable facts)
  await assertFails(aliceDb.collection("zed_knowledge").doc(validId).update({ fact: "Altered fact" }));

  // Bob cannot delete Alice's fact
  await assertFails(bobDb.collection("zed_knowledge").doc(validId).delete());

  // Admin can delete fact
  await assertSucceeds(adminDb.collection("zed_knowledge").doc(validId).delete());
});

test("Admin routine publish & book catalog security bounds enforced", async () => {
  const adminDb = getDb(testEnv.authenticatedContext("admin_uid", { email: ADMIN_EMAIL }));
  const studentDb = getDb(testEnv.authenticatedContext("stu_uid", { email: USER_EMAIL }));

  await assertFails(studentDb.collection("routines").doc("master_6m_published").set({ status: "PUBLISHED" }));
  await assertSucceeds(adminDb.collection("routines").doc("master_6m_published").set({ status: "PUBLISHED" }));
});
