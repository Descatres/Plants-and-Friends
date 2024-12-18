import { useNavigate } from "react-router-dom";

import classes from "./LandingPage.module.css";
import Button from "../../Components/Buttons/Button";
import {
  LOGIN_ROUTE,
  REGISTER_ROUTE,
} from "../../utils/routesAndEndpoints/routesAndEndpoints";

function LandingPage() {
  const navigate = useNavigate();

  const handleNavigateLogin = () => {
    navigate(LOGIN_ROUTE);
  };
  const handleNavigateRegister = () => {
    navigate(REGISTER_ROUTE);
  };

  return (
    <div className={classes.formContainer}>
      <div className={classes.form}>
        <div style={{ display: "flex", flexDirection: "column" }}>
          <h1>Rooted in Care </h1>
          <h1 style={{ textAlign: "right" }}>&emsp;Growing with Precision</h1>
        </div>
        <div style={{ maxWidth: "40rem", minWidth: "20rem" }}>
          <p>
            &ensp;Plant pantry that tracks soil humidity, temperature, and plant
            details, offering real-time insights for optimal growth.
          </p>
        </div>
        <div className={classes.buttonsContainer}>
          <Button variant="secondary" onClick={handleNavigateLogin}>
            Login
          </Button>
          <p>or</p>
          <p style={{ cursor: "pointer" }} onClick={handleNavigateRegister}>
            Register
          </p>
        </div>
      </div>
    </div>
  );
}

export default LandingPage;
