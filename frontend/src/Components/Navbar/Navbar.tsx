import React from "react";
import classes from "./Navbar.module.css";
import Button from "../Buttons/Button";

function Navbar() {
  return (
    <div className={classes.navbar}>
      <img src="/plant.svg" alt="plant" width="50px" />
      <h1>Plant&Friends</h1>
      <Button>Login</Button>
    </div>
  );
}

export default Navbar;
