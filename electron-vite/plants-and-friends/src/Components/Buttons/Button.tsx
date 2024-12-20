import React, { ReactNode } from "react";
import classes from "./Button.module.css";

interface ButtonProps {
  variant?: "primary" | "secondary" | "tertiary" | "danger";
  onClick?: () => void;
  disabled?: boolean;
  children: ReactNode;
}

function Button({
  variant = "primary",
  onClick,
  disabled,
  children,
}: ButtonProps) {
  return (
    <button
      className={
        variant === "primary"
          ? classes.primary
          : variant === "secondary"
          ? classes.secondary
          : variant === "danger"
          ? classes.danger
          : classes.tertiary
      }
      disabled={disabled}
      onClick={onClick}
    >
      {children}
    </button>
  );
}

export default Button;
