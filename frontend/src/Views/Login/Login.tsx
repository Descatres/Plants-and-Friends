import { useState } from "react";
import { useNavigate } from "react-router-dom";
import classes from "./Login.module.css";
import Input from "../../Components/InputFields/Input";

function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    try {
      const response = await fetch("http://localhost:5001/auth", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem("token", data.token);
        window.location.replace("/home");
      } else {
        const data = await response.json();
        setError(data.error);
      }

      const data = await response.json();
      setSuccess("Login successful!");
      console.log("Token:", data.token);
    } catch (err: any) {
      setError(err.message || "An unexpected error occurred");
    }
  };

  // const handleNavigateLogin = () => {
  //   navigate("/login");
  // };
  // const handleNavigateRegister = () => {
  //   navigate("/register");
  // };

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
            Login
          </button>
        </form>
        {error && <div className={classes.error}>{error}</div>}
        {success && <div className={classes.success}>{success}</div>}
      </div>
    </div>
  );
}

export default Login;
