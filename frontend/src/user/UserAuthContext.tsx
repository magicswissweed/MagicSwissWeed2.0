import {createContext, useContext, useEffect, useState} from "react";
import {
    createUserWithEmailAndPassword,
    GoogleAuthProvider,
    sendPasswordResetEmail,
    signInWithEmailAndPassword,
    signInWithPopup,
    signOut,
    User,
} from "firebase/auth";
import {firebaseAuth} from '../firebase/FirebaseConfig';
import {UserApi} from '../gen/msw-api-ts';
import {authConfiguration} from '../api/config/AuthConfiguration';


// @ts-ignore
const userAuthContext: Context<any> = createContext();

export function useUserAuth() {
    return useContext(userAuthContext);
}

// @ts-ignore
export function UserAuthContextProvider({children}) {
    // null if not logged in, undefined if not yet known
    const [user, setUser] = useState<User | null | undefined>(undefined);
    const [token, setToken] = useState<string | null>(null);

    useEffect(() => {
        const unsubscribe = firebaseAuth.onAuthStateChanged((currentuser) => {
            if (currentuser) {
                currentuser.getIdToken(false).then(async (token) => {
                    authConfiguration(token).then((config) => {
                        new UserApi(config).registerUser().then(() => { // Maybe register, maybe downsync, maybe nothing
                            setToken(token) // token is set after making sure that the user is created in the backend
                            setUser(currentuser);
                        });
                    });
                })
            } else {
                setUser(currentuser);
            }
        });

        return () => {
            unsubscribe();
        };
    }, []);

    return (
        <userAuthContext.Provider
            value={{user, token, logIn, signUp, logOut, sendForgotPasswordEmail, googleSignIn}}
        >
            {children}
        </userAuthContext.Provider>
    );

    async function logIn(email: string, password: string) {
        return signInWithEmailAndPassword(firebaseAuth, email, password);
    }

    async function signUp(email: string, password: string): Promise<void> {
        let user = await createUserWithEmailAndPassword(firebaseAuth, email, password);
        let token = await user.user.getIdToken(false);
        let config = await authConfiguration(token);
        await new UserApi(config).registerUser();
    }

    async function sendForgotPasswordEmail(email: string): Promise<void> {
        await sendPasswordResetEmail(firebaseAuth, email);
    }

    async function logOut() {
        await signOut(firebaseAuth);
        document.location.reload();
    }

    function googleSignIn() {
        const googleAuthProvider = new GoogleAuthProvider();
        return signInWithPopup(firebaseAuth, googleAuthProvider);
    }
}
