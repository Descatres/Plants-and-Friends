import React, { useState } from "react";
import classes from "./Login.module.css";

function Login() {
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

	return (
		<div className={classes.mainContainer}>
			<div className={classes.loginForm}>
				<h1>Login</h1>
				<form onSubmit={handleLogin}>
					<div className={classes.formGroup}>
						<label htmlFor='email'>Email</label>
						<input type='email' id='email' value={email} onChange={(e) => setEmail(e.target.value)} required />
					</div>
					<div className={classes.formGroup}>
						<label htmlFor='password'>Password</label>
						<input type='password' id='password' value={password} onChange={(e) => setPassword(e.target.value)} required />
					</div>
					<button type='submit' className={classes.loginButton}>
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
