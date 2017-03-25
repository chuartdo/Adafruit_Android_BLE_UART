package com.chuart.util.libs;
/**
 * Created by @Chuartdo on 2/3/2017.
 * Expose Ble UART methods to Unity
 */

import com.unity3d.player.UnityPlayer;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

public final class UnityUARTPlugin {
static final String TAG = "bleController";

    public static UnityUARTPlugin getInstance() {

        UnityUARTPlugin plugin = instance;
        if (plugin == null) {
            synchronized (SINGLETON_LOCK) {
                plugin = instance;
                if (plugin == null) {
                    dataBuffer = new byte[20];
                    plugin = new UnityUARTPlugin();
                    plugin.bleManager = new BluetoothLeUart(UnityPlayer.currentActivity.getApplicationContext());
                    instance = plugin;
                }
            }
        }
        return plugin;
    }


    public void disconnect() {
        Log.d(TAG, "Terminating Ble Connection.");
        bleManager.unregisterCallback(callback);  // todo - fix
        bleManager.disconnect();
    }


    public void connectBLEController(String options) {
        Log.d(TAG, "Register bleManager callback");
        bleManager.registerCallback(callback);
        Log.d(TAG, "Connect to Ble");

        bleManager.connectFirstAvailable();
     }

    public static short toInt(byte[] bytes, int offset) {
        short ret = 0;
        for (int i=0; i<2 && i+offset<bytes.length; i++) {
            ret <<= 8;
            ret |= (int)bytes[i] & 0xFF;
        }
        return ret;
    }

    BluetoothLeUart.Callback callback = new BluetoothLeUart.Callback() {
        @Override
        public void onConnected(BluetoothLeUart uart) {
            UnityPlayer.UnitySendMessage("BleController", "BleStatus", "connected");
        }

        @Override
        public void onConnectFailed(BluetoothLeUart uart) {
            UnityPlayer.UnitySendMessage("BleController", "BleStatus", "failed");
        }

        @Override
        public void onDisconnected(BluetoothLeUart uart) {
            UnityPlayer.UnitySendMessage("BleController", "BleStatus", "disconnected");
        }

        @Override
        public void onReceive(BluetoothLeUart uart, BluetoothGattCharacteristic rx) {

            byte[] data = rx.getValue();

            if (data != null ) {
                //dataBuffer[0] = (byte)data.length;
                for (int i=0; i< data.length; i++) {
                    dataBuffer[i] = data[i];
                }

            } else
                dataBuffer[0] = 0;
        }

        @Override
        public void onDeviceFound(BluetoothDevice device) {

        }

        @Override
        public void onDeviceInfoAvailable() {

        }
    };

    public byte[] getDataBuffer() {
        if (bleManager == null) {
            return null;
        }

        return dataBuffer;
    }

    public void sendData(byte[] command) {
        Log.d(TAG, "send " + command.length);
        bleManager.send(command);
    }



    /* create singleton object */
    static private byte[] dataBuffer;
    private static volatile UnityUARTPlugin instance;
    private static final Object SINGLETON_LOCK = new Object();

    private UnityUARTPlugin() {
    }

    private BluetoothLeUart bleManager;


    /**
     * Stores callback information
     */
    private static class UnityCallback {
        private final String gameObjectName;
        private final String methodName;

        UnityCallback(String gameObjectName, String methodName) {
            this.gameObjectName = gameObjectName;
            this.methodName = methodName;
        }

        public String getGameObjectName() {
            return gameObjectName;
        }

        public String getMethodName() {
            return methodName;
        }

        @Override
        public int hashCode() {
            return (gameObjectName + methodName).hashCode();
        }
    }

}
