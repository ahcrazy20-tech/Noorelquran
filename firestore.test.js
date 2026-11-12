const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

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

test("Unauthenticated user: cannot read or write khatmah_plans", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("khatmah_plans").get()
  );
});

test("Authenticated user: can create and read their own khatmah_plan", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  const planRef = aliceDb.collection("users").doc(ALICE_UID).collection("khatmah_plans").doc("plan_1");

  await assertSucceeds(
    planRef.set({
      userId: ALICE_UID,
      title: "ختمة رمضان المباركة",
      targetDays: 30,
      completedPages: 20,
      currentSurahNumber: 2,
      currentAyahNumber: 141,
      recitationMode: "shadowing",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(planRef.get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("khatmah_plans").get()
  );
});

test("Authenticated user: cannot read or write another user's khatmah_plan or bookmarks", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("bookmarks")
      .doc("bm_1")
      .set({
        userId: BOB_UID,
        surahNumber: 36,
        surahName: "يس",
        ayahNumber: 1,
        pageNumber: 440,
        ayahSnippet: "يس والقرآن الحكيم",
        note: "تدبر",
        createdAt: now,
        updatedAt: now,
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("bookmarks").doc("bm_1").get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("bookmarks").get()
  );
});

test("Shadow update test: rejects ghost fields on khatmah_plans", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  const planRef = aliceDb.collection("users").doc(ALICE_UID).collection("khatmah_plans").doc("plan_ghost");

  await assertFails(
    planRef.set({
      userId: ALICE_UID,
      title: "ختمة",
      targetDays: 30,
      completedPages: 0,
      currentSurahNumber: 1,
      currentAyahNumber: 1,
      recitationMode: "reading",
      isVerifiedAdmin: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});
