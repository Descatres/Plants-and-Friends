import { ChangeEvent } from "react";
import classes from "./Input.module.css";

interface InputProps {
  label?: string;
  type: string;
  id: string;
  value: string;
  onChange: (e: ChangeEvent<HTMLInputElement>) => void;
  required: boolean;
}

function Input({
  label,
  type,
  id,
  value,
  onChange,
  required = false,
}: InputProps) {
  return (
    <div className={classes.formGroup}>
      <label htmlFor={id}>{label}</label>
      <input
        className={classes.input}
        type={type}
        id={id}
        value={value}
        onChange={onChange}
        required={required}
      ></input>
    </div>
  );
}

export default Input;
