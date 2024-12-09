import { useState } from "react";
import classes from "./Navbar.module.css";
import Button from "../Buttons/Button";
import bell from "../../assets/bell.svg";
import menu from "../../assets/menu.svg";
import { useNavigate } from "react-router-dom";

function Navbar() {
  const navigate = useNavigate();
  const [isLogged, setIsLogged] = useState<boolean>(true);

  const handleNavigateLandingPage = () => {
    navigate("/");
  };

  // TODO check if user is logged in and navigate accordingly
  const handleNavigateLogin = () => {
    navigate("/login");
  };

  const handleNavigateHome = () => {
    isLogged && navigate("/home");
  };

  return (
    <div className={classes.navbar}>
      <div className={classes.logoTitle}>
        <img
          onClick={handleNavigateLandingPage}
          style={{ cursor: "pointer" }}
          src="/plant.svg"
          alt="plant"
          width="50px"
        />
        <h1
          onClick={handleNavigateHome}
          style={{ cursor: isLogged ? "pointer" : "default " }}
        >
          Plants&Friends
        </h1>
      </div>
      {isLogged ? (
        <div className={classes.buttons}>
          <img src={bell} alt="bell" width="30px" />
          <img src={menu} alt="menu" width="30px" />
        </div>
      ) : (
        <Button onClick={handleNavigateLogin}>Login</Button>
      )}
    </div>
  );
}

export default Navbar;
