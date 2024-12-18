import React, { ReactNode } from "react";
import classes from "./Button.module.css";

interface ButtonProps {
  variant?: "primary" | "secondary" | "tertiary";
  onClick?: () => void;
  children: ReactNode;
}

function Button({ variant = "primary", onClick, children }: ButtonProps) {
  return (
    <button
      className={
        variant === "primary"
          ? classes.primary
          : variant === "secondary"
          ? classes.secondary
          : classes.tertiary
      }
      onClick={onClick}
    >
      {children}
    </button>
  );
}

export default Button;
