import { useNavigate } from "react-router-dom";

import classes from "./LandingPage.module.css";
import Button from "../../Components/Buttons/Button";

function LandingPage() {
  const navigate = useNavigate();

  const handleNavigateLogin = () => {
    navigate("/login");
  };
  const handleNavigateRegister = () => {
    navigate("/register");
  };

  return (
    <div className={classes.formContainer}>
      <div className={classes.form}>
        <h1>Rooted in Care Growing with Precision</h1>
        <p>
          Plant pantry that tracks soil humidity, temperature, and plant
          details, offering real-time insights for optimal growth.
        </p>
        <div className={classes.buttonsContainer}>
          <>
            <Button variant="secondary" onClick={handleNavigateLogin}>
              Login
            </Button>
            <p>or</p>
            <p onClick={handleNavigateRegister}>Register</p>
          </>
        </div>
      </div>
    </div>
  );
}

export default LandingPage;
