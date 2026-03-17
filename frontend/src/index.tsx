import "./index.scss"
import "bootstrap/dist/css/bootstrap.min.css";
import "@firebase-oss/ui-styles/dist.min.css";
import React from 'react';
import {FirebaseUIProvider} from "@firebase-oss/ui-react";
import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import ReactDOM from 'react-dom/client';
import {firebaseUI} from './firebase/FirebaseConfig';
import {UserAuthContextProvider} from './user/UserAuthContext';
import {MswOverviewPage} from './overview/MswOverviewPage';
import {ErrorNotFound} from "./error/404";
import {AuthModalProvider} from './user/AuthModalContext';
import {GoogleMapsProvider} from "./map-provider/GoogleMapsProvider";
import {MswThemeProvider} from "./theme/MswThemeProvider";

const root = ReactDOM.createRoot(
    document.getElementById('root') as HTMLElement,
);

root.render(
    <UserAuthContextProvider>
        <FirebaseUIProvider ui={firebaseUI}>
            <AuthModalProvider>
                <GoogleMapsProvider>
                    <MswThemeProvider>
                        <BrowserRouter>
                            <Routes>
                                <Route path="/" element={<Navigate replace to="/spots"/>}/>
                                <Route path="/spots" element={<MswOverviewPage/>}/>
                                <Route path="*" element={<ErrorNotFound/>}/>
                            </Routes>
                        </BrowserRouter>
                    </MswThemeProvider>
                </GoogleMapsProvider>
            </AuthModalProvider>
        </FirebaseUIProvider>
    </UserAuthContextProvider>,
);
