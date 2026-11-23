import { useState } from "react";

import App from "./App.jsx";
import RoleManagementPage from "./RoleManagementPage.jsx";
import PortalPage from "./PortalPage.jsx";
import ManagementPage from "./ManagementPage.jsx";
import AnalyticsPage from "./AnalyticsPage.jsx";

// Tabs across the top so everything lives in one React app.
export default function Shell() {
    const [page, setPage] = useState("users");

    return (
        <>
            <nav className="tabs">
                <button
                    className={page === "users" ? "" : "secondary"}
                    onClick={() => setPage("users")}
                >
                    User Management
                </button>
                <button
                    className={page === "roles" ? "" : "secondary"}
                    onClick={() => setPage("roles")}
                >
                    Role Management
                </button>
                <button
                    className={page === "portal" ? "" : "secondary"}
                    onClick={() => setPage("portal")}
                >
                    Service Portal
                </button>
                <button
                    className={page === "management" ? "" : "secondary"}
                    onClick={() => setPage("management")}
                >
                    Service Management
                </button>
                <button
                    className={page === "analytics" ? "" : "secondary"}
                    onClick={() => setPage("analytics")}
                >
                    Service Analytics
                </button>
            </nav>

            {page === "users" && <App />}
            {page === "roles" && <RoleManagementPage />}
            {page === "portal" && <PortalPage />}
            {page === "management" && <ManagementPage />}
            {page === "analytics" && <AnalyticsPage />}
        </>
    );
}
