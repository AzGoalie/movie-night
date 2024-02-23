import { createContext, PropsWithChildren, useEffect, useState } from "react";
import { onAuthStateChanged, signInAnonymously, User } from "firebase/auth";

import { auth } from "../firebase.ts";

export const Authentication = createContext<User | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [pending, setPending] = useState<boolean>(true);

  useEffect(() => {
    signInAnonymously(auth).catch((error) =>
      console.error("Failed to sign in anonymously", error),
    );

    return onAuthStateChanged(auth, (user) => {
      setPending(false);
      setCurrentUser(user);
    });
  }, []);

  if (!pending) {
    return (
      <Authentication.Provider value={currentUser}>
        {children}
      </Authentication.Provider>
    );
  }
}
