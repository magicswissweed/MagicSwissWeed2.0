import "./index.scss"
import "bootstrap/dist/css/bootstrap.min.css";
import React from 'react';
import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import ReactDOM from 'react-dom/client';
import {UserAuthContextProvider} from './user/UserAuthContext';
import {MswOverviewPage} from './overview/MswOverviewPage';
import {ErrorNotFound} from "./error/404";
import {AuthModalProvider} from './user/AuthModalContext';
import {GoogleMapsProvider} from "./map-provider/GoogleMapsProvider";

const root = ReactDOM.createRoot(
    document.getElementById('root') as HTMLElement,
);

root.render(
    <UserAuthContextProvider>
        <AuthModalProvider>
            <GoogleMapsProvider>
                <BrowserRouter>
                    <Routes>
                        <Route path="/" element={<Navigate replace to="/spots"/>}/>
                        <Route path="/spots" element={<MswOverviewPage/>}/>
                        <Route path="*" element={<ErrorNotFound/>}/>
                    </Routes>
                </BrowserRouter>
            </GoogleMapsProvider>
        </AuthModalProvider>
    </UserAuthContextProvider>,
);
