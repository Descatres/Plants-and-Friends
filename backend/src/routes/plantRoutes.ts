import { getPlants, getPlant, postPlant, patchPlant } from "../controllers/plantController";
import { authenticate } from "../middleware/authMiddleware";

const express = require("express");
const router = express.Router();

router.get("/", authenticate, getPlants);
router.get("/plant/:id", authenticate, getPlant);
router.post("/", authenticate, postPlant);
router.patch("/plant/:id", authenticate, patchPlant);

export default router;
