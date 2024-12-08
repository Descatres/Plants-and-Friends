import { useState, useEffect } from "react";
import {
  BrowserRouter as Router,
  Route,
  Routes,
  RouterProvider,
} from "react-router-dom";
import Home from "./Views/Home/Home";
import Login from "./Views/Login/Login";
import Register from "./Views/Register/Register";
import Plant from "./Views/Plant/Plant";
import Navbar from "./Components/Navbar/Navbar";
import LandingPage from "./Views/LandingPage/LandingPage";

function App() {
  const [isLogged, setIsLogged] = useState(true);

  useEffect(
    // if path is "/" and isLogged is true, redirect to "/home"
    () => {
      if (window.location.pathname === "/" && isLogged) {
        window.location.pathname = "/home";
      }
    },
    [isLogged]
  );

  return (
    <Router>
      <Navbar />
      <div style={{ padding: "2rem 0" }}>
        <Routes>
          {isLogged ? (
            <>
              <Route path="/home" element={<Home />} />
              <Route path="/login" element={<Login />} /> {/* Adicionei isto aqui para testar o login, depois é preciso remover e dar fix do login no frontend */}
              <Route path="/plant-information:id" element={<Plant />} />
            </>
          ) : (
            <>
              <Route path="/" element={<LandingPage />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
            </>
          )}
        </Routes>
      </div>
    </Router>
  );
}

export default App;