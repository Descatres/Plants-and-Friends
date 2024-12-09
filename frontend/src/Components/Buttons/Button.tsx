import React, { ReactNode } from "react";
import classes from "./Button.module.css";

interface ButtonProps {
  variant?: "primary" | "secondary";
  onClick?: () => void;
  children: ReactNode;
}

function Button({ variant = "primary", onClick, children }: ButtonProps) {
  return (
    <button
      className={variant === "primary" ? classes.primary : classes.secondary}
      onClick={onClick}
    >
      {children}
    </button>
  );
}

export default Button;
