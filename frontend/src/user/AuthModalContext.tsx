import React, { createContext, useContext, useState } from 'react';

interface AuthModalContextType {
    showLoginModal: boolean;
    showSignupModal: boolean;
    showForgotPasswordModal: boolean;
    setShowLoginModal: (show: boolean) => void;
    setShowSignupModal: (show: boolean) => void;
    setShowForgotPasswordModal: (show: boolean) => void;
}

const AuthModalContext = createContext<AuthModalContextType | undefined>(undefined);

export function useAuthModal() {
    const context = useContext(AuthModalContext);
    if (context === undefined) {
        throw new Error('useAuthModal must be used within an AuthModalProvider');
    }
    return context;
}

export function AuthModalProvider({ children }: { children: React.ReactNode }) {
    const [showLoginModal, setShowLoginModal] = useState(false);
    const [showSignupModal, setShowSignupModal] = useState(false);
    const [showForgotPasswordModal, setShowForgotPasswordModal] = useState(false);

    const value = {
        showLoginModal,
        showSignupModal,
        showForgotPasswordModal,
        setShowLoginModal,
        setShowSignupModal,
        setShowForgotPasswordModal
    };

    return (
        <AuthModalContext.Provider value={value}>
            {children}
        </AuthModalContext.Provider>
    );
} 