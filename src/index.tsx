import { NativeModules, Platform } from 'react-native';
const { PermissionFile } = NativeModules;
const LINKING_ERROR =
  `The package 'react-native-android-scoped-storage' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const AndroidScopedStorage = NativeModules.AndroidScopedStorage
  ? NativeModules.AndroidScopedStorage
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export async function imageCapture() {
  return await PermissionFile.accessStorage(1);
}

export async function imagePicker() {
  return await PermissionFile.accessStorage(2);
}

export async function videoCapture() {
  return await PermissionFile.accessStorage(3);
}

export async function videoPicker() {
  return await PermissionFile.accessStorage(4);
}
