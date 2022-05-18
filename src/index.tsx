import { NativeModules } from 'react-native';
const { PermissionFile } = NativeModules;

 
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