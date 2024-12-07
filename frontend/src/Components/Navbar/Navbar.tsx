import React, { useState } from "react";
import classes from "./Navbar.module.css";
import Button from "../Buttons/Button";
import bell from "../../assets/bell.svg";
import menu from "../../assets/menu.svg";

function Navbar() {
  const [isLogged, setIsLogged] = useState(true);
  return (
    <div className={classes.navbar}>
      <img src="/plant.svg" alt="plant" width="50px" />
      <h1>Plants&Friends</h1>
      {isLogged ? (
        <div className={classes.buttons}>
          <img src={bell} alt="bell" width="30px" />
          <img src={menu} alt="menu" width="30px" />
        </div>
      ) : (
        <Button>Login</Button>
      )}
    </div>
  );
}

export default Navbar;
