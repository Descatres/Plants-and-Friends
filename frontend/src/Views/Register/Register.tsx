import { FormEvent, useState } from "react";
import classes from "./Register.module.css";
import Input from "../../Components/InputFields/Input";
import { useAuthentication } from "../../hooks/useAuthentication";
import Spinner from "../../Components/Spinner/Spinner";
import { useNavigate } from "react-router-dom";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();

  const { login, isLoading } = useAuthentication();

  const handleRegister = async (e: FormEvent) => {
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    e.preventDefault();
    const data = { email, password };
    login(data);
  };

  const handleNavigateToLogin = () => {
    navigate("/login");
  };

  return (
    <div className={classes.formContainer}>
      <div className={classes.form}>
        <form onSubmit={handleRegister}>
          <Input
            label="Email"
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <Input
            label="Password"
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <Input
            label="Confirm your password"
            type="password"
            id="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />
          <div className={classes.buttonContainer}>
            <button type="submit" className={classes.loginButton}>
              {isLoading ? <Spinner /> : "Register"}
            </button>
            <p
              style={{ cursor: "pointer", alignSelf: "center" }}
              onClick={handleNavigateToLogin}
            >
              or login to your account
            </p>
          </div>
        </form>
      </div>
    </div>
  );
}

export default Login;
