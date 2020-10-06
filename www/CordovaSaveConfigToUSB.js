var exec = require('cordova/exec');

exports.saveCfgToUsb = function (arg0, success, error) {
    exec(success, error, 'CordovaSaveConfigToUSB', 'saveCfgToUsb', [arg0]);
};
