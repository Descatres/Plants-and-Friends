import { useState } from "react";
import classes from "./Navbar.module.css";
import Button from "../Buttons/Button";
import bell from "../../assets/bell.svg";
import menu from "../../assets/menu.svg";
import { useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import { RootState } from "../../store/store";

function Navbar() {
  const navigate = useNavigate();
  const token = useSelector((state: RootState) => state.token.value);

  const handleNavigateLogin = () => {
    navigate("/login");
  };

  const handleNavigateRegister = () => {
    navigate("/register");
  };

  const handleNavigateHome = () => {
    if (token) navigate("/home");
    else navigate("/");
  };

  return (
    <div className={classes.navbar}>
      <div className={classes.logoTitle}>
        <img
          onClick={handleNavigateHome}
          style={{ cursor: "pointer" }}
          src="/plant.svg"
          alt="plant"
          width="50px"
        />
        <h1 onClick={handleNavigateHome} style={{ cursor: "pointer" }}>
          Plants&Friends
        </h1>
      </div>
      {token ? (
        <div className={classes.buttons}>
          <img src={bell} alt="bell" width="30px" />
          <img src={menu} alt="menu" width="30px" />
        </div>
      ) : (
        <Button
          onClick={
            window.location.pathname !== "/login"
              ? handleNavigateLogin
              : handleNavigateRegister
          }
        >
          {window.location.pathname !== "/login" ? "Login" : "Register"}
        </Button>
      )}
    </div>
  );
}

export default Navbar;
