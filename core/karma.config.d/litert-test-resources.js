const fs = require("fs");
const path = require("path");

const target = __dirname.includes(`${path.sep}wasm${path.sep}`) ? "wasmJs" : "js";
const modelPath = path.resolve(
    __dirname,
    `../../../../core/build/processedResources/${target}/test/CelsiusToFahrenheit.tflite`
);

if (!fs.existsSync(modelPath)) {
    throw new Error("Could not locate the CelsiusToFahrenheit.tflite browser test resource");
}

config.files.push({
    pattern: modelPath,
    included: false,
    served: true,
    watched: false
});

config.set({
    basePath: path.resolve(__dirname, "../../../../"),
    client: {
        mocha: {
            timeout: 60000
        }
    },
    browserNoActivityTimeout: 60000,
    browserDisconnectTimeout: 60000,
    pingTimeout: 60000
});
