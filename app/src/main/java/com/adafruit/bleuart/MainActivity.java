package com.adafruit.bleuart;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements BluetoothLeUart.Callback {

    // UI elements
    private TextView messages;
    private EditText input;

    // Bluetooth LE UART instance.  This is defined in BluetoothLeUart.java.
    private BluetoothLeUart uart;

    // Write some text to the messages text view.
    // Care is taken to do this on the main UI thread so writeLine can be called from any thread
    // (like the BTLE callback).
    private void writeLine(final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.append(text);
                messages.append("\n");
            }
        });
    }

    // Handler for mouse click on the send button.
    public void sendClick(View view) {
        String message = input.getText().toString();
        uart.send(message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grab references to UI elements.
        messages = (TextView) findViewById(R.id.messages);
        input = (EditText) findViewById(R.id.input);

        // Initialize UART.
        uart = new BluetoothLeUart(getApplicationContext());
    }

    // OnCreate, called once to initialize the activity.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // OnResume, called right before UI is displayed.  Connect to the bluetooth device.
    @Override
    protected void onResume() {
        super.onResume();
        writeLine("Connecting...");
        uart.registerCallback(this);
        uart.connectFirstAvailable();
    }

    // OnStop, called right before the activity loses foreground focus.  Close the BTLE connection.
    @Override
    protected void onStop() {
        super.onStop();
        uart.unregisterCallback(this);
        uart.disconnect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // UART Callback event handlers.
    @Override
    public void onConnected(BluetoothLeUart uart) {
        // Called when UART device is connected and ready to send/receive data.
        writeLine("Connected!");
    }

    @Override
    public void onConnectFailed(BluetoothLeUart uart) {
        // Called when some error occured which prevented UART connection from completing.
        writeLine("Error connecting to device!");
    }

    @Override
    public void onDisconnected(BluetoothLeUart uart) {
        // Called when the UART device disconnected.
        writeLine("Disconnected!");
    }

    @Override
    public void onReceive(BluetoothLeUart uart, BluetoothGattCharacteristic rx) {
        // Called when data is received by the UART.
        writeLine("Received: " + rx.getStringValue(0));
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        // Called when a UART device is discovered (after calling startScan).
        writeLine("Found device: " + device.getAddress());
    }
}