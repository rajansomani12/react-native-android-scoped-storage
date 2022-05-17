import { NativeModules, Platform } from 'react-native';

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

export function multiply(a: number, b: number): Promise<number> {
  return AndroidScopedStorage.multiply(a, b);
}
