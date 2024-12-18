import { getNotifications } from "../controllers/notificationController";
import { authenticate } from "../middleware/authMiddleware";

const express = require("express");

const router = express.Router();

router.get("/notifications", authenticate, getNotifications);

export default router;
