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
import LoggedOutWrapper from "./utils/wrappers/LoggedOutWrapper";

function App() {
  const [isLogged, setIsLogged] = useState(false);

  // useEffect(
  //   () => {
  //     if (window.location.pathname === "/" && isLogged) {
  //       window.location.pathname = "/home";
  //     }
  //   },
  //   [isLogged]
  // );

  return (
    <Router>
      <Navbar />
      <div>
        <Routes>
          {/* {isLogged ? ( */}
          <>
            <Route path="/home" element={<Home />} />
            <Route path="/plant-information:id" element={<Plant />} />
          </>
          {/* ) : ( */}
          <>
            <Route path="/" element={<LoggedOutWrapper />}>
              <Route path="/" element={<LandingPage />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
            </Route>
          </>
          {/* )} */}
        </Routes>
      </div>
    </Router>
  );
}

export default App;
