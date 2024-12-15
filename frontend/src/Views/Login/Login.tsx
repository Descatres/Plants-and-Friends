import { FormEvent, useState } from "react";
import classes from "./Login.module.css";
import Input from "../../Components/InputFields/Input";
import { removeToken, setToken } from "../../store/slices/tokenSlice";
import { useAuthentication } from "../../hooks/useAuthentication";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);

  const { login, isLoading } = useAuthentication();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    const data = { email, password };
    login(data);
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
          <Input
            label="Password"
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <button type="submit" className={classes.loginButton}>
            {isLoading ? "Logging in..." : "Login"}
          </button>
        </form>
        {error && <div className={classes.error}>{error}</div>}
        {/* {success && <div className={classes.success}>{success}</div>} */}
      </div>
    </div>
  );
}

export default Login;
