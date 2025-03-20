package chess.com;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.animation.ObjectAnimator;
import android.animation.ArgbEvaluator;
import android.graphics.Color;
import android.view.animation.LinearInterpolator;
import android.widget.TimePicker;
import android.widget.Toast;

import android.media.SoundPool;
import android.media.AudioAttributes;
import android.util.SparseIntArray;

import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.content.SharedPreferences;

public class Ok extends Activity {
	private TextView timerPlayer1, timerPlayer2, moveCountPlayer1, moveCountPlayer2;
	private ImageView playPauseButton, resetButton, setTimeButton, soundToggleButton, colorButton;
	private RelativeLayout player1Layout, player2Layout;
	private CountDownTimer countDownTimer1, countDownTimer2;
	private boolean isPlayer1Active = false, isPlayer2Active = false, isSoundOn = true;
	private long timeLeftInMillis = 60000;
	private long timeLeftPlayer1, timeLeftPlayer2;
	private int movesPlayer1 = 0, movesPlayer2 = 0;
	private MediaPlayer soundPlayer;

	private String activeColor = "#FF3B30";
	private String inactiveColor = "#1C1C1C";

	private SoundPool soundPool;
	private SparseIntArray soundIds = new SparseIntArray(); // To store multiple sound IDs
	private boolean isSoundLoaded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Yu.setVisi(getWindow());
		setContentView(R.layout.k);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		timerPlayer1 = findViewById(R.id.timerPlayer1);
		timerPlayer2 = findViewById(R.id.timerPlayer2);
		moveCountPlayer1 = findViewById(R.id.moveCountPlayer1);
		moveCountPlayer2 = findViewById(R.id.moveCountPlayer2);
		playPauseButton = findViewById(R.id.playPauseButton);
		resetButton = findViewById(R.id.resetButton);
		setTimeButton = findViewById(R.id.setTimeButton);
		colorButton = findViewById(R.id.colorButton);
		soundToggleButton = findViewById(R.id.soundToggleButton);
		player1Layout = findViewById(R.id.player1Layout);
		player2Layout = findViewById(R.id.player2Layout);

		try {
			Long timeLeft = Long.valueOf(Yu.readFile(Ok.this, "leftTime"));
			if (timeLeft != 0) {
				timeLeftInMillis = timeLeft;
			}
		} catch (Exception e) {
			//Yu.showToast(this, "Invalid format : " + e.getMessage());
		}

		try {
			String color = Yu.readFile(this, "color");
			if (!color.isEmpty()) {
				activeColor = color;
			}
		} catch (Exception e) {
			//Yu.showToast(this, "Invalid color format : " + e.getMessage());
		}

		resetClocks();

		playPauseButton.setOnClickListener(v -> togglePlayPause());
		resetButton.setOnClickListener(
				v -> showCustomDialog("Reset Timer", "Are you sure you want to reset the timers?", false));
		setTimeButton.setOnClickListener(v -> showAdjustTimeDialog(this));
		soundToggleButton.setOnClickListener(v -> toggleSound());
		colorButton.setOnClickListener(v -> showColorDialog());

		player1Layout.setOnTouchListener((v, event) -> {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onPlayerLayoutTap(2);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				break;
			}
			return true;
		});

		player2Layout.setOnTouchListener((v, event) -> {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onPlayerLayoutTap(1);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				break;
			}
			return true;
		});

		AudioAttributes audioAttributes = new AudioAttributes.Builder()
				.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_GAME).build();

		soundPool = new SoundPool.Builder().setMaxStreams(5) // You can increase this number if needed
				.setAudioAttributes(audioAttributes).build();

		soundIds.put(R.raw.Ck, soundPool.load(this, R.raw.Ck, 1));
		soundIds.put(R.raw.rh, soundPool.load(this, R.raw.rh, 1));
		soundIds.put(R.raw.yF, soundPool.load(this, R.raw.yF, 1));
		soundIds.put(R.raw.nV, soundPool.load(this, R.raw.nV, 1));
		soundIds.put(R.raw.GQ, soundPool.load(this, R.raw.GQ, 1));

		soundPool.setOnLoadCompleteListener((sp, id, status) -> {
			if (status == 0) {
				isSoundLoaded = true;
			}
		});

		playSound(R.raw.Ck);
	}

	public void showAdjustTimeDialog(Context context) {
		Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout._st);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

		EditText hourInput = dialog.findViewById(R.id.hour_input);
		EditText minuteInput = dialog.findViewById(R.id.minute_input);
		EditText secondInput = dialog.findViewById(R.id.second_input);

		TextView cancelButton = dialog.findViewById(R.id.cancel_button);
		TextView saveButton = dialog.findViewById(R.id.save_button);

		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
			}
		});

		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int hours = parseInput(hourInput);
				int minutes = parseInput(minuteInput);
				int seconds = parseInput(secondInput);
				long timeLeft = (hours * 3600 + minutes * 60 + seconds) * 1000;
				timeLeftInMillis = timeLeft;
				Yu.writeFile(Ok.this, "leftTime", String.valueOf(timeLeft));

				if (isPlayer1Active || isPlayer2Active) {
					showCustomDialog("Reset Timer", "Are you sure you want to reset the timers?", false);
				} else {
					resetClocks();
					playSound(R.raw.nV);
				}

				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private static int parseInput(EditText input) {
		String inputText = input.getText().toString().trim();
		return inputText.isEmpty() ? 0 : Integer.parseInt(inputText);
	}

	public void resetClock() {
		timeLeftInMillis = 0;
	}

	public long getTimeLeftInMillis() {
		return timeLeftInMillis;
	}

	private void showTimeOverDialog(int player) {
		String message = "Time over for Player " + (player == 2 ? "1" : "2") + "!" + "& Green winn!";
		showCustomDialog("Game Over", message, false);
	}

	private void showCustomDialog(String title, String discr, boolean isErr) {
		Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout._go);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		TextView titleTxt = dialog.findViewById(R.id.dialogTitle);
		TextView description = dialog.findViewById(R.id.dialogDescription);
		titleTxt.setText(title);
		description.setText(discr);

		TextView actionButton = dialog.findViewById(R.id.btnAction);
		actionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isErr) {
					resetClocks();
					playSound(R.raw.nV);
				}
				dialog.dismiss();
			}
		});

		TextView cancelButton = dialog.findViewById(R.id.btnCancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private void onPlayerLayoutTap(int player) {
		if (player == 1 && !isPlayer1Active) {
			startPlayerTimer(2);
		} else if (player == 2 && !isPlayer2Active) {
			startPlayerTimer(1);
		}
	}

	private void togglePlayPause() {
		if (isPlayer1Active) {
			pauseTimer(1);
			setPlayPauseButtonImage(R.drawable.ps);
		} else if (isPlayer2Active) {
			pauseTimer(2);
			setPlayPauseButtonImage(R.drawable.ps);
		} else {
			if (isPlayer1Active) {
				startTimer(1);
				updateUIForPlayer(1);
				setPlayPauseButtonImage(R.drawable.pl);
			} else {
				startTimer(1);
				updateUIForPlayer(2);
				setPlayPauseButtonImage(R.drawable.pl);
			}
		}

		playSound(R.raw.rh);
	}

	private void setPlayPauseButtonImage(int resId) {
		playPauseButton.setImageResource(resId);
	}

	private void startPlayerTimer(int player) {
		if (player == 1) {
			pauseTimer(1);
			startTimer(player);
			movesPlayer1++;
			moveCountPlayer1.setText("P1 Moves : " + movesPlayer1);
			updateUIForPlayer(2);
		} else {
			pauseTimer(2);
			startTimer(player);
			movesPlayer2++;
			moveCountPlayer2.setText("P2 Moves : " + movesPlayer2);
			updateUIForPlayer(1);
		}
		setPlayPauseButtonImage(R.drawable.pl);
		playSound(R.raw.yF);
	}

	private void startTimer(int player) {
		if (player == 1) {
			countDownTimer2 = new CountDownTimer(timeLeftPlayer2, 1000) {
				public void onTick(long millisUntilFinished) {
					timeLeftPlayer2 = millisUntilFinished;
					updateTimerText(timerPlayer2, timeLeftPlayer2);
				}

				public void onFinish() {
					isPlayer2Active = false;
					player2Layout.setBackgroundColor(getResources().getColor(R.color.red));
					player1Layout.setBackgroundColor(getResources().getColor(R.color.colorGreen));
					timerPlayer1.setTextColor(getResources().getColor(R.color.white));
					showTimeOverDialog(1);
				}
			}.start();
			isPlayer2Active = true;
		} else {
			countDownTimer1 = new CountDownTimer(timeLeftPlayer1, 1000) {
				public void onTick(long millisUntilFinished) {
					timeLeftPlayer1 = millisUntilFinished;
					updateTimerText(timerPlayer1, timeLeftPlayer1);
				}

				public void onFinish() {
					isPlayer1Active = false;
					player1Layout.setBackgroundColor(getResources().getColor(R.color.red));
					player2Layout.setBackgroundColor(getResources().getColor(R.color.colorGreen));
					timerPlayer2.setTextColor(getResources().getColor(R.color.white));
					showTimeOverDialog(2);
				}
			}.start();
			isPlayer1Active = true;
		}
	}

	private void pauseTimer(int player) {
		if (player == 1 && countDownTimer1 != null) {
			countDownTimer1.cancel();
			isPlayer1Active = false;
		} else if (player == 2 && countDownTimer2 != null) {
			countDownTimer2.cancel();
			isPlayer2Active = false;
		}
	}

	private void updateUIForPlayer(int player) {
		if (player == 1) {
			timerPlayer1.setTextColor(getResources().getColor(R.color.white));
			timerPlayer2.setTextColor(getResources().getColor(R.color.colorGray));
			animateBackgroundColor(player1Layout, activeColor);
			animateBackgroundColor(player2Layout, inactiveColor);
		} else {
			timerPlayer2.setTextColor(getResources().getColor(R.color.white));
			timerPlayer1.setTextColor(getResources().getColor(R.color.colorGray));
			animateBackgroundColor(player2Layout, activeColor);
			animateBackgroundColor(player1Layout, inactiveColor);
		}
	}

	private void animateBackgroundColor(final RelativeLayout layout, final String colorCode) {
		int targetColor = Color.parseColor(colorCode);
		int currentColor = Color.WHITE;
		if (layout.getBackground() instanceof ColorDrawable) {
			currentColor = ((ColorDrawable) layout.getBackground()).getColor();
		}
		ObjectAnimator colorAnim = ObjectAnimator.ofObject(layout, "backgroundColor", new ArgbEvaluator(), currentColor,
				targetColor);
		colorAnim.setDuration(200);
		colorAnim.setInterpolator(new LinearInterpolator());
		colorAnim.start();
	}

	private int parseColorFromString(String colorCode) {
		int color = 0;
		if (colorCode != null && !colorCode.isEmpty()) {
			try {
				if (colorCode.startsWith("#")) {
					color = Color.parseColor(colorCode);
				} else {
					int colorResId = getResources().getIdentifier(colorCode, "color", getPackageName());
					if (colorResId != 0) {
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
							color = getResources().getColor(colorResId, getTheme());
						} else {
							color = getResources().getColor(colorResId);
						}
					} else {
						Yu.showToast(this, "Color resource not found: " + colorCode);
					}
				}
			} catch (IllegalArgumentException e) {
				Yu.showToast(this, "Invalid color format: " + e.getMessage());
			}
		} else {
			Yu.showToast(this, "Color code is null or empty");
		}
		return color;
	}

	private void toggleSound() {
		isSoundOn = !isSoundOn;
		if (isSoundOn) {
			soundToggleButton.setImageResource(R.drawable.son);
		} else {
			soundToggleButton.setImageResource(R.drawable.soff);
		}
		playSound(R.raw.rh);
	}

	/*
	public void playSound(int soundResourceId) {
		if (isSoundOn) {
			if (soundPlayer == null) {
				soundPlayer = MediaPlayer.create(this, soundResourceId);
				} else {
				soundPlayer.reset();
				soundPlayer = MediaPlayer.create(this, soundResourceId);
			}
			
			if (soundPlayer != null) {
				soundPlayer.setOnCompletionListener(mp -> {
					mp.release();
					soundPlayer = null;
				});
				soundPlayer.start();
			}
		}
	}
	*/

	public void playSound(int soundResourceId) {
		if (isSoundOn && isSoundLoaded) {
			Integer soundId = soundIds.get(soundResourceId);
			if (soundId != null) {
				soundPool.play(soundId, 1, 1, 1, 0, 1);
			}
		}
	}

	private void resetClocks() {
		timeLeftPlayer1 = timeLeftInMillis;
		timeLeftPlayer2 = timeLeftInMillis;
		movesPlayer1 = 0;
		movesPlayer2 = 0;
		if (countDownTimer1 != null) {
			countDownTimer1.cancel();
		}
		if (countDownTimer2 != null) {
			countDownTimer2.cancel();
		}
		isPlayer1Active = false;
		isPlayer2Active = false;
		updateTimerText(timerPlayer1, timeLeftPlayer1);
		updateTimerText(timerPlayer2, timeLeftPlayer2);
		moveCountPlayer1.setText("P1 Moves : " + movesPlayer1);
		moveCountPlayer2.setText("P2 Moves : " + movesPlayer2);
		int targetColor = parseColorFromString(inactiveColor);

		player1Layout.setBackgroundColor(Color.parseColor(inactiveColor));
		player2Layout.setBackgroundColor(Color.parseColor(inactiveColor));

		playPauseButton.setImageResource(R.drawable.ps);
		if (soundPlayer != null) {
			soundPlayer.stop();
		}
	}

	private void updateTimerText(TextView timerTextView, long timeInMillis) {
		int seconds = (int) (timeInMillis / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		String timeFormatted = String.format("%02d:%02d", minutes, seconds);
		timerTextView.setText(timeFormatted);
	}

	////

	private void showColorDialog() {
		List<String> colorList = generateRandomColors();

		LayoutInflater inflater = LayoutInflater.from(Ok.this);
		View view = inflater.inflate(R.layout._pkc, null);

		ListView listView = view.findViewById(R.id.colorListView);

		// Using a custom adapter for better performance
		ColorAdapter adapter = new ColorAdapter(colorList);
		listView.setAdapter(adapter);

		AlertDialog.Builder builder = new AlertDialog.Builder(Ok.this);
		builder.setView(view);

		AlertDialog dialog = builder.create();
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		dialog.show();

		listView.setOnItemClickListener((parent, view1, position, id) -> {
			String selectedColor = colorList.get(position);
			activeColor = selectedColor;
			Yu.writeFile(this, "color", selectedColor);
			//Yu.showToast(this, selectedColor + " copied to clipboard");
			playSound(R.raw.nV);
			dialog.dismiss();
		});
	}

	private class ColorAdapter extends BaseAdapter {
		private List<String> colorList;
		private LayoutInflater inflater;

		public ColorAdapter(List<String> colorList) {
			this.colorList = colorList;
			this.inflater = LayoutInflater.from(Ok.this);
		}

		@Override
		public int getCount() {
			return colorList.size();
		}

		@Override
		public Object getItem(int position) {
			return colorList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, android.view.ViewGroup parent) {
			ViewHolder holder;

			// Reusing views with ViewHolder pattern
			if (convertView == null) {
				convertView = inflater.inflate(R.layout._pkc_it, parent, false);
				holder = new ViewHolder();
				holder.colorTextView = convertView.findViewById(R.id.colorText);
				convertView.setTag(holder); // Set ViewHolder as the tag for convertView
			} else {
				holder = (ViewHolder) convertView.getTag(); // Retrieve ViewHolder from convertView
			}

			String colorCode = colorList.get(position);

			// Set color as background color and add rounded border
			holder.colorTextView.setBackground(createRoundedBorderDrawable(colorCode));

			return convertView;
		}

		// ViewHolder pattern to cache the views for each item
		class ViewHolder {
			TextView colorTextView;
		}

		private Drawable createRoundedBorderDrawable(String colorCode) {
			GradientDrawable drawable = new GradientDrawable();
			drawable.setShape(GradientDrawable.RECTANGLE);
			drawable.setCornerRadius(10); // Adjust corner radius as needed
			drawable.setStroke(2, Color.BLACK); // Border color and width
			drawable.setColor(Color.parseColor(colorCode)); // Set the background color to the passed color

			return drawable;
		}
	}

	private List<String> generateRandomColors() {
		List<String> colorList = new ArrayList<>();
		Random random = new Random();
		for (int i = 0; i < 30; i++) {
			int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
			String colorCode = String.format("#%06X", (0xFFFFFF & color));
			colorList.add(colorCode);
		}
		return colorList;
	}

	/////

}

/*




*/