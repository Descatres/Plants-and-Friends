import { GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader";
import * as THREE from "three";

class ModelLoader {
  private static instance: THREE.Object3D | null = null;

  static loadModel(url: string): Promise<THREE.Object3D> {
    return new Promise((resolve, reject) => {
      if (this.instance) {
        resolve(this.instance);
      } else {
        const loader = new GLTFLoader();
        loader.load(
          url,
          (gltf) => {
            this.instance = gltf.scene;
            resolve(gltf.scene);
          },
          undefined,
          (error) => {
            reject(error);
          }
        );
      }
    });
  }
}

export default ModelLoader;
