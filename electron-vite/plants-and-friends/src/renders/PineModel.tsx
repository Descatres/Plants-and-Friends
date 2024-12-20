import { memo, useEffect, useState } from "react";
import { Canvas, useFrame } from "@react-three/fiber";
import { OrbitControls } from "@react-three/drei";
import ModelLoader from "./ModelLoader";
import * as THREE from "three";

function PineModel() {
  const [model, setModel] = useState<THREE.Object3D | null>(null);

  useEffect(() => {
    ModelLoader.loadModel(new URL("../assets/Pine.glb", import.meta.url).href)
      .then((loadedModel) => {
        setModel(loadedModel);
      })
      .catch((error) => {
        console.error("Error loading model:", error);
      });
  }, []);

  useFrame(() => {
    if (model) {
      model.rotation.y += 0.001;
    }
  });
  // check if it is a mobile device ande remove the model

  return model ? <primitive object={model} scale={3} /> : null;
}

const PineModelMemo = memo(PineModel);

function TreeCanvas() {
  return (
    <Canvas>
      <ambientLight intensity={2.5} />
      <directionalLight position={[1000, 1000, 1000]} />
      <PineModelMemo />
      <OrbitControls />
    </Canvas>
  );
}

export default TreeCanvas;
