package statz.fun;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.IBinder;

public class JumpService extends Service {
	int mStartMode;       // indicates how to behave if the service is killed
	IBinder mBinder;      // interface for clients that bind
	boolean mAllowRebind; // indicates whether onRebind should be used
	private MediaPlayer _mediaPlayer;
	private SensorManager _sensorManager;
	private float _accel; // acceleration apart from gravity
	private float _accelCurrent; // current acceleration including gravity
	private float _accelLast; // last acceleration including gravity
	private float _highest;
	private float _lowest;
	private float _absThreshold = 4;
	private static final int jumpId = 99;
	private static JumpService JUMPER;

	@Override
	public void onCreate() {
		_highest = 0;
		_lowest = 0;
		_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		_mediaPlayer = MediaPlayer.create(this, R.raw.mb_jump);
		_sensorManager.registerListener(_sensorListener, _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		JUMPER = this;
	}
	
	public static JumpService getInstance(){
		return JUMPER;
	}
	
	public float getAbsThreshold(){
		return _absThreshold;
	}
	
	public void setAbsThreshold(float threshold){
		_absThreshold = threshold;
	}
	
	public void playAudio(){
		_mediaPlayer.start();
	}
	private final SensorEventListener _sensorListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent se) {
			float x = se.values[0];
			float y = se.values[1];
			float z = se.values[2];
			_accelLast = _accelCurrent;
			_accelCurrent = (float) Math.sqrt((x*x + y*y + z*z));
			float delta = _accelCurrent - _accelLast;
			_accel = _accel * 0.9f + delta; // perform low-cut filter

			if(_highest < _accel)
				_highest = _accel;
			if(_lowest > _accel)
				_lowest = _accel;

			if(Math.abs(_highest) > _absThreshold && Math.abs(_lowest) > _absThreshold){
				playAudio();
				_lowest = 0;
				_highest = 0;
			}

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
				new Intent(getApplicationContext(), Costumer.class),
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification();
		notification.tickerText = "Your costume is ready!";
		notification.icon = R.drawable.icon;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.setLatestEventInfo(getApplicationContext(), "Mario Bros Costume", "You can jump now", pi);
		startForeground(jumpId, notification);
		return mStartMode;
	}
	@Override
	public IBinder onBind(Intent intent) {
		// A client is binding to the service with bindService()
		return mBinder;
	}
	@Override
	public boolean onUnbind(Intent intent) {
		// All clients have unbound with unbindService()
		return mAllowRebind;
	}
	@Override
	public void onRebind(Intent intent) {
		// A client is binding to the service with bindService(),
		// after onUnbind() has already been called
	}
	@Override
	public void onDestroy() {
		_sensorManager.unregisterListener(_sensorListener);
		if (_mediaPlayer != null) {
			_mediaPlayer.release();
			_mediaPlayer = null;
		}
	}
}
