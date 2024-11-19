import React from "react";
import classes from "./Button.module.css";

interface ButtonProps {
  variant?: "primary" | "secondary";
  onClick?: () => void;
  children: React.ReactNode;
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
