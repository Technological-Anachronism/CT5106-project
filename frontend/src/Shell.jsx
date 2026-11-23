import { useState } from "react";

import App from "./App.jsx";
import ManagementPage from "./ManagementPage.jsx";
import RoleManagementPage from "./RoleManagementPage.jsx";

// Shared navigation. Other dashboards are unchanged.
export default function Shell() {
    const [page, setPage] = useState("users");

    return <>
        <nav className="tabs">
            <button className={page === "users" ? "" : "secondary"} onClick={() => setPage("users")}>User Management</button>
            <button className={page === "roles" ? "" : "secondary"} onClick={() => setPage("roles")}>Role Management</button>
            <button className={page === "management" ? "" : "secondary"} onClick={() => setPage("management")}>Service Management</button>
        </nav>
        {page === "users" ? <App /> : page === "roles" ? <RoleManagementPage /> : <ManagementPage />}
    </>;
}
