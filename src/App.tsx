import { useState, useEffect } from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import { Plant } from "./Types/Plant";
import "./App.css";

function App() {
    return (
        <Router>
            <Routes>
                <Route
                    path="/"
                    // element={
                    //     <Home
                    //         plants={plants}
                    //     />
                    // }
                />
            </Routes>
        </Router>
    );
}

export default App;
