# react-native-android-scoped-storage

Scoped storage for android 10 and above version 

## Installation

```sh
npm install react-native-android-scoped-storage
```

## Usage

```js
import React, {useState, useEffect} from 'react';
import * as picker from 'react-native-android-scoped-storage';
import {
  SafeAreaView,
  Text,
  TouchableOpacity,
  View,
  NativeModules,
  NativeEventEmitter,
  Image,
} from 'react-native';
const {PermissionFile} = NativeModules;
const App = () => {
  var [response, setResponse] = useState({});
  
  async function imageVideoPicker() {

     let eventEmitter = null;
     let imageListener = null;
     let videoListener = null;
     if (Platform.OS == 'android') {
       eventEmitter = new NativeEventEmitter(PermissionFile);
       imageListener = eventEmitter.addListener('imageData', imageData => {
         imageData.map(item => {
           setResponse(item);
           imageListener.remove();
           return;
        
         });
       });
       videoListener = eventEmitter.addListener('videoData', videoData => {
         videoData.map((item, index) => {
           setResponse(item);
           videoListener.remove();
           return;
           
         });
       });
     }

    picker.imageCapture();
  }

  return (
    <SafeAreaView
      style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
      <View>
        <TouchableOpacity onPress={() => imageVideoPicker()}>
          <Text>click me</Text>
        </TouchableOpacity>
        <Image
          source={{uri: response.path}}
          style={{height: 100, width: 100}}
        />
      </View>
    </SafeAreaView>
  );
};

export default App;

// ...


```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
