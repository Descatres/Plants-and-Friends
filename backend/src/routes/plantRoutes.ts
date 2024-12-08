import { getPlants, getPlant, postPlant } from "../controllers/plantController";
import { authenticate } from "../middleware/authMiddleware";

const express = require("express");
const router = express.Router();

router.get("/", authenticate, getPlants);
router.get("/:id", authenticate, getPlant);
router.post("/", authenticate, postPlant);

export default router;
