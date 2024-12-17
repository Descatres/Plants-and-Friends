import { useEffect } from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Home from "./Views/Home/Home";
import Login from "./Views/Login/Login";
import Register from "./Views/Register/Register";
import Plant from "./Views/Plant/Plant";
import Navbar from "./Components/Navbar/Navbar";
import LandingPage from "./Views/LandingPage/LandingPage";
import LoggedOutWrapper from "./utils/wrappers/LoggedOutWrapper";
import {
  HOME_ROUTE,
  PLANT_ROUTE,
  LOGIN_ROUTE,
  REGISTER_ROUTE,
  LANDING_PAGE_ROUTE,
} from "./utils/routesAndEndpoints/routesAndEndpoints";
import { useSelector } from "react-redux";
import { RootState } from "./store/store";
import { ToastContainer } from "react-toastify";

function App() {
  const token = useSelector((state: RootState) => state.token.value);

  useEffect(() => {
    if (
      (window.location.pathname === "" || window.location.pathname === "/") &&
      token
    ) {
      window.location.pathname = HOME_ROUTE;
    }

    if (
      window.location.pathname !== LANDING_PAGE_ROUTE &&
      window.location.pathname != LOGIN_ROUTE &&
      window.location.pathname !== REGISTER_ROUTE &&
      !token
    ) {
      window.location.pathname = LANDING_PAGE_ROUTE;
    }
  }, [token, window.location.pathname]);

  return (
    <Router>
      <Navbar />
      <ToastContainer position="top-right" autoClose={2000} />
      <div>
        <Routes>
          {token ? (
            <>
              <Route path={HOME_ROUTE} element={<Home />} />
              <Route path={PLANT_ROUTE} element={<Plant />} />
            </>
          ) : (
            <>
              <Route path={LANDING_PAGE_ROUTE} element={<LoggedOutWrapper />}>
                <Route path={LANDING_PAGE_ROUTE} element={<LandingPage />} />
                <Route path={LOGIN_ROUTE} element={<Login />} />
                <Route path={REGISTER_ROUTE} element={<Register />} />
              </Route>
            </>
          )}
        </Routes>
      </div>
    </Router>
  );
}

export default App;
