import { getNotifications, deleteAllNotifications } from "../controllers/notificationController";
import { authenticate } from "../middleware/authMiddleware";

const express = require("express");

const router = express.Router();

router.get("/notifications", authenticate, getNotifications);
router.delete("/notifications", authenticate, deleteAllNotifications);

export default router;
