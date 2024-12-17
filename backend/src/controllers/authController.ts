import { Response } from "express";
import { loginUser, registerUser } from "../services/authService";
import { CustomRequest } from "../types/CustomRequest";
import { CustomError } from "../utils/CustomError";

async function login(req: CustomRequest, res: Response) {
	const { email, password } = req.body;

	if (!email || !password) {
		throw new CustomError("Email and password are required", 400);
	}

	const { token, refreshToken } = await loginUser(email, password.trim());

	res.cookie("refreshToken", refreshToken, {
		httpOnly: true,
		secure: process.env.NODE_ENV === "production",
		maxAge: 1 * 24 * 60 * 60 * 1000,
	});

 	res.json({ token, message: "Login successful" });
}

async function register(req: CustomRequest, res: Response) {
	const { email, password } = req.body;

	if (!email || !password) {
		throw new CustomError("Email and password are required", 400);
	}

	const user = await registerUser(email, password.trim());
	res.status(201).json({ user, message: "Registration successful" });
}

async function logout(req: CustomRequest, res: Response) {
	if (!req.session) {
		throw new CustomError("No active session", 400);
	}

	req.session.destroy((err) => {
		if (err) {
			throw new CustomError(err.message || "Logout failed", 500);
		}
		res.clearCookie("connect.sid");
		res.json({ message: "Logout successful" });
	});
}

export { login, register, logout };
