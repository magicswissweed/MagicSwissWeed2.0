import {initializeApp} from "firebase/app";
import {initializeUI} from "@firebase-oss/ui-core";
import {browserLocalPersistence, getAuth, setPersistence} from "firebase/auth";
import 'firebase/compat/auth';
import 'firebase/compat/firestore';

// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
const firebaseConfig = {
    apiKey: process.env.REACT_APP_APIKEY,
    authDomain: process.env.REACT_APP_AUTHDOMAIN,
    projectId: process.env.REACT_APP_PROJECTID,
    storageBucket: process.env.REACT_APP_STORAGEBUCKET,
    messagingSenderId: process.env.REACT_APP_MESSAGINGSENDERID,
    appId: process.env.REACT_APP_APPID
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

const auth = getAuth(app);

const ui = initializeUI({app});

setPersistence(auth, browserLocalPersistence);
export const firebaseApp = app;
export const firebaseAuth = auth;
export const firebaseUI = ui;
