import { useState, useEffect } from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import "./App.css";

function App() {
  const [isLogged, setIsLogged] = useState(false);

  return (
    <Router>
      <Routes>
        {isLogged ? (
          <>
            <Route
              path="/home"
              // element={
              //     <Home
              //         plants={plants}
              //     />
              // }
            />
            <Route
              path="/plant-information:id"
              // element={
              //     <Home
              //         plants={plants}
              //     />
              // }
            />
          </>
        ) : (
          <>
            <Route
              path="/"
              // element={
              //     <Home
              //         plants={plants}
              //     />
              // }
            />
            <Route
              path="/login"
              // element={
              //     <Home
              //         plants={plants}
              //     />
              // }
            />
            <Route
              path="/register"
              // element={
              //     <Home
              //         plants={plants}
              //     />
              // }
            />
          </>
        )}
      </Routes>
    </Router>
  );
}

export default App;
