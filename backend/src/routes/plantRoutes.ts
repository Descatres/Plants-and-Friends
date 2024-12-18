import { getPlants, getPlant, postPlant, patchPlant, removePlant } from "../controllers/plantController";
import { authenticate } from "../middleware/authMiddleware";

const express = require("express");
const router = express.Router();

router.get("/", authenticate, getPlants);
router.get("/plant/:id", authenticate, getPlant);
router.post("/", authenticate, postPlant);
router.patch("/plant/:id", authenticate, patchPlant);
router.delete("/plant/:id", authenticate, removePlant);

export default router;
