import { login, register, logout } from "../controllers/authController";
import { authenticate, refreshToken } from "../middleware/authMiddleware";

const passport = require("passport");
const express = require("express");

const router = express.Router();

router.post("/auth", login);
router.post("/auth/register", register);
router.post("/auth/logout", authenticate, logout);

router.get("/auth/google", passport.authenticate("google", { scope: ["profile", "email"] }));

router.get("/auth/google/callback", passport.authenticate("google", { failureRedirect: "/login" }), (req: { user: any; }, res: { json: (arg0: { message: string; user: any; }) => void; }) => {
	res.json({ message: "Google login successful", user: req.user });
});

router.post("/refresh", refreshToken);

export default router;
