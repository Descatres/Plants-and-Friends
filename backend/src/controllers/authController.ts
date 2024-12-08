import { Request, Response, NextFunction } from "express";
import { loginUser } from "../services/authService";

async function login(req: Request, res: Response, next: NextFunction) {
	const { email, password } = req.body;

	if (!email || !password) {
		return next(new Error("Email and password are required"));
	}

	loginUser(email, password.trim())
		.then((token) => res.json({ token, message: "Login successful" }))
		.catch(next);
}

export { login }