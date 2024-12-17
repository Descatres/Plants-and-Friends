import jwt from "jsonwebtoken";
import User from "../models/User";
import { CustomError } from "../utils/CustomError";

const JWT_SECRET = process.env.JWT_SECRET || "your_jwt_secret";
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || "your_jwt_refresh_secret";

async function loginUser(email: string, password: string) {
	const user = await User.findOne({ email: email });
	if (!user) {
		throw new CustomError("Invalid email or password", 401);
	}

	const isMatch = await user.comparePassword(password);
	if (!isMatch.valueOf()) {
		throw new CustomError("Invalid email or password", 401);
	}

	const token = jwt.sign({ id: user._id }, JWT_SECRET, { expiresIn: "1h" });;

	const refreshToken = jwt.sign({ id: user._id }, JWT_REFRESH_SECRET, { expiresIn: "1d" });

	return { token, refreshToken };
}

async function registerUser(email: string, password: string) {
	const existingUser = await User.findOne({ email });
	if (existingUser) {
		throw new CustomError("Email already in use", 400);
	}

	const user = new User({email, password });
	await user.save();

	return { id: user._id, email: user.email };
}

export { loginUser, registerUser };