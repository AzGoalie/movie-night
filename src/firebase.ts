import { initializeApp } from "firebase/app";
import { connectFirestoreEmulator, getFirestore } from "firebase/firestore";
import { connectAuthEmulator, getAuth } from "firebase/auth";

import { emulators } from "../firebase.json";

const firebaseConfig = {
  apiKey: "AIzaSyCKtJVUHHDRF8OYbE8onqJJI3nsAXvsP7A",
  authDomain: "movie-night-2928a.firebaseapp.com",
  projectId: "movie-night-2928a",
  storageBucket: "movie-night-2928a.appspot.com",
  messagingSenderId: "170551763745",
  appId: "1:170551763745:web:a86648ec2bdfa5bff35460",
};

const app = initializeApp(firebaseConfig);

export const auth = getAuth(app);
export const db = getFirestore(app);

if (import.meta.env.DEV) {
  connectAuthEmulator(auth, `http://localhost:${emulators.auth.port}`);
  connectFirestoreEmulator(db, "localhost", emulators.firestore.port);
}
