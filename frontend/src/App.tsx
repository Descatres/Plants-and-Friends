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
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "./store/store";
import { removeToken } from "./store/slices/tokenSlice";

function App() {
  const token = useSelector((state: RootState) => state.token.value);
  // const dispatch = useDispatch();
  // dispatch(removeToken(null));

  useEffect(() => {
    if (
      (window.location.pathname === "/" || window.location.pathname === "") &&
      token
    ) {
      window.location.pathname = "/home";
    }
  }, [token, window.location.pathname]);

  return (
    <Router>
      <Navbar />
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
