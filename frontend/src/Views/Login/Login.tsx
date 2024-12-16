import { FormEvent, useState } from "react";
import classes from "./Login.module.css";
import Input from "../../Components/InputFields/Input";
import { useAuthentication } from "../../hooks/useAuthentication";
import Spinner from "../../Components/Spinner/Spinner";
import { useNavigate } from "react-router-dom";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const navigate = useNavigate();

  const { login, isLoading } = useAuthentication();

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault();
    const data = { email, password };
    login(data);
  };

  const handleNavigateToRegister = () => {
    navigate("/register");
  };

  const handleResetPassword = () => {
    console.log("Reset password");
  };

  return (
    <div className={classes.formContainer}>
      <div className={classes.form}>
        <form onSubmit={handleLogin}>
          <Input
            label="Email"
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <div>
            <Input
              label="Password"
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <p
              style={{ cursor: "pointer", alignSelf: "center" }}
              onClick={handleResetPassword}
            >
              Reset password
            </p>
          </div>
          <div className={classes.buttonContainer}>
            <button type="submit" className={classes.loginButton}>
              {isLoading ? <Spinner /> : "Login"}
            </button>
            <p
              style={{ cursor: "pointer", alignSelf: "center" }}
              onClick={handleNavigateToRegister}
            >
              or create your account
            </p>
          </div>
        </form>
      </div>
    </div>
  );
}

export default Login;
