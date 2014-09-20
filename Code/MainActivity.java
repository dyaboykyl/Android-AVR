package com.example.simplesocket;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This application sets up a server side socket on port 38300.  The application sends color intensity
 * values to the client to modulate an RGB LED. 
 * 
 * This code was heavily borrowed from this website: http://www.florescu.org/archives/2010/10/15/android-usb-connection-to-pc/comment-page-1/
 * Thank you Alex Florescu.
 * 
 * @author Zack 
 *
 */

public class MainActivity extends Activity implements OnClickListener, OnSeekBarChangeListener {

	public static final int TIMEOUT = 6;

	private String connectionStatus = null;
	private String socketData = null;
	private Handler mHandler = null;
	ServerSocket server = null;
	boolean running = false;

	public static boolean connected;
	public static Scanner socketIn;
	public static BufferedOutputStream socketOut;

	int red;
	int blue;
	int green;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up click listeners for the buttons
		View connectButton = findViewById(R.id.connect_button);
		connectButton.setOnClickListener(this);
		View closeBtn = findViewById(R.id.close);
		closeBtn.setOnClickListener(this);

		//Set up listeners for the seekbars
		SeekBar seekBar = (SeekBar) findViewById(R.id.red_bar);
		seekBar.setOnSeekBarChangeListener(this);
		TextView tv = (TextView) findViewById(R.id.red_text);
		tv.setText("Red Intensity: "+seekBar.getProgress());
		
		seekBar = (SeekBar)findViewById(R.id.blue_bar);
		seekBar.setOnSeekBarChangeListener(this);
		tv = (TextView) findViewById(R.id.blue_text);
		tv.setText("Blue Intensity: "+seekBar.getProgress());
		
		seekBar = (SeekBar)findViewById(R.id.green_bar);
		seekBar.setOnSeekBarChangeListener(this);
		tv = (TextView) findViewById(R.id.green_text);
		tv.setText("Green Intensity: "+seekBar.getProgress());

		mHandler = new Handler();
	}

	/**
	 * Button click implementations
	 */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.connect_button:
			// initialize server socket in a new separate thread
			new Thread(initializeConnection).start();
			String msg = "Attempting to connect...";
			Toast.makeText(this, msg, msg.length()).show();
			break;		
		case R.id.close:
			//Close connection
			if (socketOut != null) {
				try {
					socketOut.write('q');				
					socketOut.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}		
			}
			running = false;
			break;
		default:
			break;
		}
	}

	/**
	 * SeekBar listener implementation
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		//Color character
		char color = 'x';
		//Color intensity
		int value = 1;
		//Color TextView
		TextView textView = null;
		String text = "";
		switch(seekBar.getId()){
		case R.id.red_bar:
			color = 'r';
			textView = (TextView) findViewById(R.id.red_text);
			text = "Red Intensity: ";
			break;

		case R.id.blue_bar:
			color = 'b';
			textView = (TextView) findViewById(R.id.blue_text);
			text = "Blue Intensity: ";
			break;

		case R.id.green_bar:
			color = 'g';
			textView = (TextView) findViewById(R.id.green_text);
			text = "Green Intensity: ";
			break;

		default:
			break;
		}
		if(color != 'x'){
			value = seekBar.getProgress() + 1;  //Add 1 so that a 0 is never sent
			if(textView != null) textView.setText(text+value);
			if (socketOut != null) {
				try {					
					socketOut.write((byte)color);
					socketOut.write((byte)value);
					socketOut.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	private Runnable initializeConnection = new Thread() {
		public void run() {

			Socket client = null;
			running = true;
			// initialize server socket
			try {
				server = new ServerSocket(38300);
				server.setSoTimeout(TIMEOUT * 1000);

				// attempt to accept a connection
				client = server.accept();
				socketIn = new Scanner(client.getInputStream());
				socketOut = new BufferedOutputStream(client.getOutputStream());
			} catch (SocketTimeoutException e) {
				// print out TIMEOUT
				connectionStatus = "Connection has timed out! Please try again";
				mHandler.post(showConnectionStatus);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// close the server socket
				try {
					if (server != null)
						server.close();
				} catch (IOException ec) {
					ec.printStackTrace();
				}
			}

			if (client != null) {
				connected = true;
				// print out success
				connectionStatus = "Connection was succesful!";
				mHandler.post(showConnectionStatus);
				while (socketIn.hasNext() && running) {
					socketData = socketIn.next();
				}				

				try {
					connectionStatus = "Connection closed";
					mHandler.post(showConnectionStatus);
					client.close();
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}
	};

	/**
	 * Pops up a "toast" to indicate the connection status
	 */
	private Runnable showConnectionStatus = new Runnable() {
		public void run() {
			Toast.makeText(getBaseContext(), connectionStatus,
					Toast.LENGTH_SHORT).show();
		}
	};





}
