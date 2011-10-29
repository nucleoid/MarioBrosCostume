package statz.fun;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.SeekBar;

public class Costumer extends Activity implements SeekBar.OnSeekBarChangeListener {
	private SeekBar _seekBar;
	private Intent _jumpIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		_jumpIntent = new Intent(this, JumpService.class);
		startService(_jumpIntent);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		_seekBar = (SeekBar)findViewById(R.id.seek);
		_seekBar.setOnSeekBarChangeListener(this);
		if(JumpService.getInstance() != null)
			_seekBar.setProgress((int)Math.round(JumpService.getInstance().getAbsThreshold() * 2));
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(_jumpIntent);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		float threshold = new Float(progress) / 2.0f;
		JumpService.getInstance().setAbsThreshold(threshold);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
}